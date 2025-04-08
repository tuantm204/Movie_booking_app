package com.example.movie_booking_app.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.Movie

@Composable
fun MovieHeader(
    movie: Movie,
    onTrailerClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        // Trailer background
        AsyncImage(
            model = movie.imagelink,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.TopCenter)
                .clickable {
                    try {
                        val trailerUrl = movie.trailer

                        if (trailerUrl.isNullOrEmpty()) {
                            Toast.makeText(context, "Không có trailer cho phim này", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }

                        onTrailerClick()
                    } catch (e: Exception) {
                        Log.e("TrailerDebug", "Lỗi khi xử lý trailer: ${e.message}", e)
                        Toast.makeText(context, "Không thể mở trailer: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        )

        // Nút xem trailer
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
                .size(40.dp)
                .border(
                    width = 3.dp,
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Xem trailer",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        // Phần nền trắng phía dưới
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.surface)
        )

        // Layout chứa poster và tiêu đề
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster phim
            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .offset(y = (-35).dp)
            ) {
                AsyncImage(
                    model = movie.imagelink,
                    contentDescription = "${movie.title} poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Tiêu đề phim và thông tin cơ bản
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, top = 0.dp)
                    .weight(1f)
            ) {
                Text(
                    text = movie.title ?: "Không có tên",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.offset(y = (-35).dp)
                )

                Row {
                    InfoChip(icon = Icons.Default.CalendarToday, text = movie.releaseDate ?: "N/A")
                    Spacer(modifier = Modifier.width(8.dp))
                    InfoChip(icon = Icons.Default.AccessTime, text = movie.duration ?: "N/A")
                }
            }
        }
    }
}