package com.example.movie_booking_app.data.model

import java.util.Date


// Rạp
data class Theater(
    val theaterId: String = "",
    val name: String = "",
    val location: String = "",
    val city: String = "",
    val contact: String = "",
    val facilities: List<String> = emptyList(),
    val openTime: String = "",
    val closeTime: String = ""
)


// Phòng chiếu
data class Room(
    val roomId: String = "",
    val theaterId: String = "",
    val name: String = "",
    val seatingCapacity: Int = 0,
    val screenType: String = "",
    val availableSeats: Int = 0,
    val facilities: List<String> = emptyList(),
    val seatMatrix: List<SeatRow> = emptyList()
)

//Hàng ghế trong phòng chiếu
data class SeatRow(
    val row: String = "",
    val types: List<String> = emptyList()
)

//Tổng hợp cho hiển thị danh sách rạp với suất chiếu
data class TheaterWithShowtimes(
    val theaterId: String,
    val name: String,
    val location: String,
    val city: String,
    val showtimes: List<Showtime>
)

//Suất chiếu
data class Showtime(
    val scheduleId: String = "",
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val roomId: String = "",
    val roomName: String = "",
    val price: Double = 0.0,
    val availableSeats: Int = 0,
    val language: String = ""
)