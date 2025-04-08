package com.example.movie_booking_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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