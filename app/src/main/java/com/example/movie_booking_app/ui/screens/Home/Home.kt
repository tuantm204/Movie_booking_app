package com.example.movie_booking_app.ui.screens.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.data.model.News
import com.example.movie_booking_app.data.repository.AuthViewModel
import com.example.movie_booking_app.data.repository.MovieViewModel
import com.example.movie_booking_app.data.repository.NewsViewModel
import com.example.movie_booking_app.ui.screens.Home.components.FeaturedNewsItem
import com.example.movie_booking_app.ui.screens.Home.components.HomeAppBar
import com.example.movie_booking_app.ui.screens.Home.components.MovieCarousel
import com.example.movie_booking_app.ui.screens.Home.components.MovieInfo
import com.example.movie_booking_app.ui.screens.Home.components.NewsCarousel
import com.example.movie_booking_app.ui.screens.Home.components.NewsSection
import com.example.movie_booking_app.ui.screens.Home.components.VideoSection
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun Home(
    movieViewModel: MovieViewModel,
    newsViewModel: NewsViewModel,
    authViewModel: AuthViewModel,
    onMovieClick: (Movie) -> Unit,
    onNewsClick: (News) -> Unit,
    onAllNewsClick: () -> Unit,
    onVideoClick: (Movie) -> Unit = {},
    onAllVideosClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val nowPlayingMovies by movieViewModel.nowPlayingMovies.collectAsState()
    val upcomingMovies by movieViewModel.upcomingMovies.collectAsState()
    val isLoading by movieViewModel.isLoading.collectAsState()
    val error by movieViewModel.error.collectAsState()
    val movies by movieViewModel.movies.collectAsState()
    val newsList by newsViewModel.news.collectAsState()

    // Theo dõi tab đang được chọn
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Đang Chiếu", "Sắp Chiếu")

    // Chọn danh sách phim dựa vào tab được chọn
    val currentMoviesList = if (selectedTabIndex == 0) nowPlayingMovies else upcomingMovies
    val originalSize = currentMoviesList.size
    val pagerState = rememberPagerState(initialPage = originalSize * 50)
    val coroutineScope = rememberCoroutineScope()

    // Xác định phim hiện tại
    val currentMovieIndex = if (originalSize > 0) {
        pagerState.currentPage % originalSize
    } else 0
    val currentMovie = currentMoviesList.getOrNull(currentMovieIndex)

    // Màn hình chính
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "res/drawable/background.png",
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(830.dp)
                .zIndex(-1f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(830.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
                .zIndex(-0.5f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .zIndex(-2f)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            HomeAppBar(
                authViewModel = authViewModel,
                onLoginClick = onLoginClick,
                onProfileClick = onProfileClick,
                onMenuClick = {}
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Đã xảy ra lỗi: $error",
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { movieViewModel.loadMovies() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                    currentMoviesList.isEmpty() -> {
                        Text(
                            "Không có phim nào",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }
                    else -> {
                        val scrollState = rememberScrollState()
                        val contentColor by remember {
                            derivedStateOf {
                                if (scrollState.value > 830 - 200) Color.Black else Color.White
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))
                            NewsCarousel(newsList, onNewsClick, newsViewModel)

                            // Tab Row
                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                        height = 3.dp,
                                        color = Color.White
                                    )
                                }
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTabIndex == index,
                                        onClick = {
                                            selectedTabIndex = index
                                            coroutineScope.launch {
                                                val newList = if (index == 0) nowPlayingMovies else upcomingMovies
                                                if (newList.isNotEmpty()) {
                                                    pagerState.scrollToPage(newList.size * 50)
                                                }
                                            }
                                        },
                                        text = {
                                            Text(
                                                text = title,
                                                fontWeight = if (selectedTabIndex == index)
                                                    FontWeight.Bold else FontWeight.Normal,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                        }
                                    )
                                }
                            }

                            MovieCarousel(currentMoviesList, movieViewModel, onMovieClick, pagerState)
                            currentMovie?.let { movie ->
                                MovieInfo(movie, movieViewModel, onMovieClick)
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            if (newsList.isNotEmpty()) {
                                val featuredNews = newsList.find { it.isPromoted } ?: newsList.first()
                                FeaturedNewsItem(
                                    news = featuredNews,
                                    onNewsClick = onNewsClick
                                )
                            }
                            NewsSection(newsList = newsList, onNewsClick = onNewsClick, onViewAllClick = onAllNewsClick)

                            val allMovies = nowPlayingMovies + upcomingMovies
                            VideoSection(movies = allMovies, onVideoClick = onVideoClick, onViewAllClick = onAllVideosClick)
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                }
            }
        }
    }
}