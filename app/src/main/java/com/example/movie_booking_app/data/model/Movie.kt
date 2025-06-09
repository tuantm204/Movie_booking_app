package com.example.movie_booking_app.data.model

//Phim
data class Movie(
    val id: String,
    val title: String = "",
    val imagelink: String = "",
    val genre: String = "",
    val duration: String = "",
    val rated: String = "",
    val status: String = "",
    val releaseDate: String = "",
    val director: String = "",
    val actors: String = "",
    val language: String = "",
    val details: String = "",
    val trailer: String = ""
)