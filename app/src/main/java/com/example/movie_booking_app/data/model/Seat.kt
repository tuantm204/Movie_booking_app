package com.example.movie_booking_app.data.model


//Ghế
data class Seat(
    val id: String,
    val row: String,
    val number: String,
    val type: SeatType,
    val status: SeatStatus = SeatStatus.AVAILABLE
)

enum class SeatType {
    STANDARD, VIP, DOUBLE
}

enum class SeatStatus {
    AVAILABLE, BOOKED, SELECTED, UNAVAILABLE
}