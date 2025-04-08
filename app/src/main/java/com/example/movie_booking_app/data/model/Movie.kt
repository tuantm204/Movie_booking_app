package com.example.movie_booking_app.data.model

data class Movie(
    val id: String = "",
    val title: String? = null,
    val director: String? = null,
    val actors: String? = null,
    val genre: String? = null,
    val releaseDate: String? = null,
    val duration: String? = null,
    val language: String? = null,
    val rated: String? = null,
    val details: String? = null,
    val trailer: String? = null,
    val imagelink: String? = null,
    val status: String? = null
)