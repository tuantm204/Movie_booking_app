package com.example.movie_booking_app.data.model

import java.util.Date

// Đánh giá phim
data class Review(
    val reviewId: String = "",
    val movieId: String = "",
    val userId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val reviewDate: Date = Date(),
    val helpfulVotes: Int = 0
)