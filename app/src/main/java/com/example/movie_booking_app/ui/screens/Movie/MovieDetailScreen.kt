package com.example.movie_booking_app.ui.screens

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
import com.example.movie_booking_app.data.model.News
import com.example.movie_booking_app.data.repository.MovieViewModel
import com.example.movie_booking_app.data.repository.NewsViewModel
import com.example.movie_booking_app.ui.screens.Home.components.NewsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit,
    movieViewModel: MovieViewModel,
    newsViewModel: NewsViewModel,
    onNewsClick: (News) -> Unit,
    onViewAllNewsClick: () -> Unit
) {
    var isTrailerPlaying by remember { mutableStateOf(false) }
    val newsList by newsViewModel.news.collectAsState()

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
                    onClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("ĐẶT VÉ")
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