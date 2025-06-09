package com.example.movie_booking_app.ui.screens.Movie

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.ui.components.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.movie_booking_app.data.model.News
import com.example.movie_booking_app.viewmodel.MovieViewModel
import com.example.movie_booking_app.viewmodel.NewsViewModel
import com.example.movie_booking_app.ui.screens.Home.components.NewsSection
import com.example.movie_booking_app.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit,
    movieViewModel: MovieViewModel,
    newsViewModel: NewsViewModel,
    reviewViewModel: ReviewViewModel,  // Thêm reviewViewModel
    onNewsClick: (News) -> Unit,
    onViewAllNewsClick: () -> Unit
) {
    var isTrailerPlaying by remember { mutableStateOf(false) }
    val newsList by newsViewModel.news.collectAsState()

    // Các state cho review
    val reviews by reviewViewModel.reviews.collectAsState()
    val filteredReviews by reviewViewModel.filteredReviews.collectAsState()
    val averageRating by reviewViewModel.averageRating.collectAsState()
    val ratingsDistribution by reviewViewModel.ratingsDistribution.collectAsState()
    val currentFilter by reviewViewModel.filterRating.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    LaunchedEffect(movie.id) {
        if (!movie.id.isNullOrEmpty()) {
            Log.d("MovieDetail", "Fetching reviews for movie: ${movie.id}")
            reviewViewModel.applyFilter(0)
            reviewViewModel.getReviewsForMovie(movie.id)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Phim") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(bottom = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (!movie.id.isNullOrBlank()) {
                            Log.d("MovieDetail", "Đặt vé cho phim: ${movie.id} - ${movie.title}")
                            onBookClick()
                        } else {
                            Log.e("MovieDetail", "Lỗi: ID phim null hoặc rỗng")
                            // Có thể hiển thị Toast thông báo lỗi
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    // Disable nút nếu ID phim không hợp lệ
                    enabled = !movie.id.isNullOrBlank()
                ) {
                    Text("ĐẶT VÉ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            MovieHeader(
                movie = movie,
                onTrailerClick = { isTrailerPlaying = true }
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp)
            ) {
                // Phần nội dung phim
                MovieContent(details = movie.details)
                Spacer(modifier = Modifier.height(16.dp))
                MovieInfoDetails(movie = movie)
            }
            // Thêm phần đánh giá
            Spacer(modifier = Modifier.height(16.dp))
            AllReviewScreen(
                reviews = reviews,
                filteredReviews = filteredReviews,
                averageRating = averageRating,
                ratingsDistribution = ratingsDistribution,
                currentFilter = currentFilter,
                isLoading = isLoading,
                onFilterChanged = { rating -> reviewViewModel.applyFilter(rating) },
                onHelpfulClick = { reviewId -> reviewViewModel.voteReviewHelpful(reviewId) }
            )
            NewsSection(
                newsList = newsList,
                onNewsClick = onNewsClick,
                onViewAllClick = onViewAllNewsClick
            )

            if (isTrailerPlaying && !movie.trailer.isNullOrEmpty()) {
                MovieTrailerDialog(
                    trailerUrl = movie.trailer!!,
                    onDismiss = { isTrailerPlaying = false }
                )
            }
        }
    }
}