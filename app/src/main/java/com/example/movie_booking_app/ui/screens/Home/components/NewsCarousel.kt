package com.example.movie_booking_app.ui.screens.Home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.News
import com.example.movie_booking_app.data.repository.MovieViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay

@Composable
fun NewsCarousel(
    newsList: List<News>,
    onNewsClick: (News) -> Unit,
    viewModel: MovieViewModel
) {
    // Giới hạn danh sách tin hiển thị chỉ lấy tối đa 10 tin đầu tiên
    val displayedNews = remember(newsList) {
        newsList.take(10)
    }

    if (displayedNews.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            // Create a separate news pager state
            val newsPagerState = rememberPagerState()

            // Auto-scroll news carousel
            LaunchedEffect(key1 = Unit) {
                while (true) {
                    delay(2000) // Delay 2 seconds between auto-scrolls
                    val nextPage = (newsPagerState.currentPage + 1) % displayedNews.size
                    newsPagerState.animateScrollToPage(nextPage)
                }
            }

            // News Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                HorizontalPager(
                    count = displayedNews.size,  // Sử dụng displayedNews thay vì newsList
                    state = newsPagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    itemSpacing = 35.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val news = displayedNews[page]  // Sử dụng displayedNews thay vì newsList

                    // News Item Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f/9f)
                            .clickable {
                                viewModel.selectNews(news)
                                onNewsClick(news)
                            }
                    ) {
                        Box {
                            // News Image
                            AsyncImage(
                                model = news.image,
                                contentDescription = news.title,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Các phần còn lại giữ nguyên
                            // ...
                        }
                    }
                }

                // Page indicators - Cũng sử dụng displayedNews
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(displayedNews.size) { iteration ->
                        val color = if (newsPagerState.currentPage == iteration)
                            Color.White else Color.White.copy(alpha = 0.3f)
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                                .size(width = 16.dp, height = 4.dp)
                        )
                    }
                }
            }
        }
    }
}