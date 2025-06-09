package com.example.movie_booking_app.data.model

//Tin tức
data class News(
    val id: String = "",
    val title: String = "",
    val time: String = "",
    val timestamp: Long = 0,
    val image: String = "",
    val bannerImage: String = "",
    val content: String = "",
    val category: String = "",
    val isPromoted: Boolean = false
)