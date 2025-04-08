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
    // Lọc các phim có trailer
    val moviesWithTrailer = movies.filter { !it.trailer.isNullOrEmpty() }

    if (moviesWithTrailer.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // Đổi màu nền bên ngoài thẻ Card
                .padding(top = 1.dp, bottom = 1.dp) // Tăng khoảng cách bên ngoài
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), // Căn lề hai bên
                colors = CardDefaults.cardColors(containerColor = Color.White) // Đổi màu nền Card về trắng
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White) // Đổi màu nền của Column về trắng
                ) {
                    // Dòng tiêu đề có nút "Tất cả"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp), // Căn lề cho nội dung bên trong
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Tiêu đề phần video
                        Text(
                            text = "Videos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // Nút "Tất cả" với viền đen
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

                    Spacer(modifier = Modifier.height(16.dp)) // Khoảng cách giữa tiêu đề và danh sách

                    // Danh sách video trailer
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp), // Căn lề cho LazyRow
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.background(Color.White) // Đổi màu nền của LazyRow về trắng
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
    // Thay thế Card đơn bằng Column để chứa cả video và thông tin
    Column(
        modifier = Modifier
            .width(178.dp)
            .padding(bottom = 8.dp)
    ) {
        // Phần video thumbnail với icon play
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Poster phim làm thumbnail
                AsyncImage(
                    model = movie.imagelink,
                    contentDescription = "${movie.title} - Trailer",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Icon play ở chính giữa
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

        // Khoảng cách giữa video và thông tin
        Spacer(modifier = Modifier.height(8.dp))

        // Phần thông tin phim bên dưới video
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            // Tiêu đề phim
            Text(
                text = movie.title ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Thông tin khởi chiếu trong một Row
            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Khởi chiếu: ",
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