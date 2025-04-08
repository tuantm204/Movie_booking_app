package com.example.movie_booking_app.ui.screens.Home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.data.repository.MovieViewModel
import com.google.accompanist.pager.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MovieCarousel(
    currentMoviesList: List<Movie>,
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit,
    pagerState: PagerState
) {
    val originalSize = currentMoviesList.size
    val coroutineScope = rememberCoroutineScope()

    // Tạo danh sách vô hạn
    val virtualInfiniteList = remember(currentMoviesList) {
        if (currentMoviesList.isNotEmpty()) {
            val repeatedList = mutableListOf<Movie>()
            repeat(100) {
                repeatedList.addAll(currentMoviesList)
            }
            repeatedList
        } else {
            emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        if (virtualInfiniteList.isNotEmpty()) {
            HorizontalPager(
                count = virtualInfiniteList.size,
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 85.dp),
                itemSpacing = (-20).dp,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val movie = virtualInfiniteList[page]

                val pageOffset = ((pagerState.currentPage - page) + pagerState
                    .currentPageOffset).absoluteValue

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val scale = lerp(
                                start = 0.75f,
                                stop = 1.0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                            scaleX = scale
                            scaleY = scale

                            alpha = lerp(
                                start = 0.7f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )

                            rotationY = lerp(
                                start = 15f,
                                stop = 0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            ) * if (pagerState.currentPage > page) -1 else 1

                            var translationZ = lerp(
                                start = -15f,
                                stop = 0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                        .fillMaxWidth(0.95f)
                        .aspectRatio(2f/3f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(Color.Transparent)
                        .clickable {
                            val realIndex = page % originalSize
                            val realMovie = currentMoviesList[realIndex]
                            viewModel.selectMovie(realMovie)
                            onMovieClick(realMovie)
                        }
                        .align(Alignment.Center)
                ) {
                    AsyncImage(
                        model = movie.imagelink,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage > virtualInfiniteList.size - originalSize * 10) {
                    pagerState.scrollToPage(originalSize * 50)
                }
                else if (pagerState.currentPage < originalSize * 10) {
                    pagerState.scrollToPage(originalSize * 50)
                }
            }
        }
    }
}