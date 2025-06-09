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
import com.example.movie_booking_app.viewmodel.NewsViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay

//10 tin tuc dau tien
@Composable
fun NewsCarousel(
    newsList: List<News>,
    onNewsClick: (News) -> Unit,
    viewModel: NewsViewModel
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
            val newsPagerState = rememberPagerState()

            LaunchedEffect(key1 = Unit) {
                while (true) {
                    delay(2000)
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
                    count = displayedNews.size,
                    state = newsPagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    itemSpacing = 35.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val news = displayedNews[page]

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
                        }
                    }
                }

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