package com.example.movie_booking_app.ui.screens.Home.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.data.repository.MovieViewModel

@Composable
fun MovieInfo(
    movie: Movie,
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit
) {
    Crossfade(
        targetState = movie,
        animationSpec = tween(durationMillis = 500)
    ) { currentMovie ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .animateContentSize()
        ) {
            // Tên phim và nút đặt vé
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentMovie.title ?: "Không có tên",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedButton(
                    onClick = {
                        viewModel.selectMovie(currentMovie)
                        onMovieClick(currentMovie)
                    },
                    modifier = Modifier.padding(start = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = Color.White
                    )
                ) {
                    Text("Đặt Vé")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentMovie.genre ?: "Không có thể loại",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Thời lượng: ${currentMovie.duration ?: "Chưa có thông tin"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}