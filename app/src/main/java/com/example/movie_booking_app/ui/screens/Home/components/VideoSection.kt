package com.example.movie_booking_app.ui.screens.Home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.Movie

@Composable
fun VideoSection(
    movies: List<Movie>,
    onVideoClick: (Movie) -> Unit,
    onViewAllClick: () -> Unit = {}
) {
    val moviesWithTrailer = movies.filter { !it.trailer.isNullOrEmpty() }

    if (moviesWithTrailer.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 1.dp, bottom = 1.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Videos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.Black),
                            modifier = Modifier.clickable { onViewAllClick() }
                        ) {
                            Text(
                                text = "Tất cả",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.background(Color.White)
                    ) {
                        items(moviesWithTrailer) { movie ->
                            VideoItem(movie = movie, onClick = { onVideoClick(movie) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(178.dp)
            .padding(bottom = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = movie.imagelink,
                    contentDescription = "${movie.title} - Trailer",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(25.dp)
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .background(color = Color.Black.copy(alpha = 0.3f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Xem trailer",
                        modifier = Modifier.size(25.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = movie.title ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "KC: ",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = movie.releaseDate ?: "Đang cập nhật",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}