package com.example.movie_booking_app.data.model

import java.util.Date

//đặt vé
data class Booking(
    val id: String = "",
    val userId: String = "",
    val movieId: String = "",
    val movieTitle: String = "",
    val movieImageUrl: String = "",
    val scheduleId: String = "",
    val showDate: String = "",
    val showTime: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val seats: List<String> = emptyList(),
    val totalPrice: Int = 0,
    val bookingTime: Date? = null,
    val status: String = "",
    
    // Thông tin thêm về rạp chiếu
    val theaterName: String = "",
    val theaterLocation: String = "",
    val screenType: String = ""
)