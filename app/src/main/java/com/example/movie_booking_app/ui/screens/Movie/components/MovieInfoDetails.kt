package com.example.movie_booking_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movie_booking_app.data.model.Movie

@Composable
fun MovieInfoDetails(movie: Movie) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CompactInfoSection("Thể loại:", movie.genre)
        CompactInfoSection("Đạo diễn:", movie.director)
        CompactInfoSection("Diễn viên:", movie.actors)
        CompactInfoSection("Ngôn ngữ:", movie.language)
        CompactInfoSection("Kiểm duyệt:", movie.rated)
    }
}

@Composable
fun CompactInfoSection(title: String, content: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = content ?: "Không có thông tin",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}