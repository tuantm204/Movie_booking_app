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
    viewModel: MovieViewModel,
    authViewModel: AuthViewModel,
    onMovieClick: (Movie) -> Unit,
    onNewsClick: (News) -> Unit,
    onAllNewsClick: () -> Unit, // Thêm tham số mới này
    onVideoClick: (Movie) -> Unit = {}, // Thêm tham số cho xử lý click video
    onAllVideosClick: () -> Unit = {}, // Thêm tham số cho nút "Tất cả" của phần video
    onLoginClick: () -> Unit = {}, // Thêm callback cho đăng nhập
    onProfileClick: () -> Unit = {} // Có thể thêm nếu cần
) {
    val nowPlayingMovies by viewModel.nowPlayingMovies.collectAsState()
    val upcomingMovies by viewModel.upcomingMovies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val newsList by viewModel.news.collectAsState()
    val movies by viewModel.movies.collectAsState()
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
        // Hình nền phủ toàn bộ màn hình
        AsyncImage(
            model = "res/drawable/background.png",
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(900.dp)
                .zIndex(-1f)
        )

        // Gradient overlay trên cùng ảnh nền để text dễ đọc
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(900.dp)
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

        // Nội dung chính của màn hình
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            HomeAppBar(
                authViewModel = authViewModel,
                onLoginClick = onLoginClick,
                onProfileClick = onProfileClick,
                onMenuClick = { /* Xử lý menu */ }
            )

            // Phần nội dung chính
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
                            Button(onClick = { viewModel.loadMovies() }) {
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))

                            // News Section
                            NewsCarousel(newsList, onNewsClick, viewModel)

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

                            // Movie Carousel
                            MovieCarousel(currentMoviesList, viewModel, onMovieClick, pagerState)

                            // Thông tin phim hiện tại
                            currentMovie?.let { movie ->
                                MovieInfo(movie, viewModel, onMovieClick)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            // Phần phim khác
                            if (newsList.isNotEmpty()) {
                                // Lấy tin tức đầu tiên hoặc tin tức được quảng bá (nếu có)
                                val featuredNews = newsList.find { it.isPromoted } ?: newsList.first()
                                FeaturedNewsItem(
                                    news = featuredNews,
                                    onNewsClick = onNewsClick
                                )
                            }
                            // Thêm phần tin tức mới ở cuối
                            NewsSection(
                                newsList = newsList,
                                onNewsClick = onNewsClick,
                                onViewAllClick = {
                                    // Điều hướng đến màn hình tất cả tin tức
                                    // Vì Home không nắm navController, bạn cần truyền callback từ MovieNavigation
                                    onAllNewsClick()
                                }
                            )
                            val allMovies = nowPlayingMovies + upcomingMovies

                            VideoSection(
                                movies = allMovies,
                                onVideoClick = onVideoClick,
                                onViewAllClick = onAllVideosClick // Truyền callback khi click "Tất cả"
                            )
                        }
                    }
                }
            }
        }
    }
}