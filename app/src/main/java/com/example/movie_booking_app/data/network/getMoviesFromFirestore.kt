package com.example.movie_booking_app.data.network
import android.util.Log
import com.example.movie_booking_app.data.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun getMoviesFromFirestore(): List<Movie> {
    val db = FirebaseFirestore.getInstance()
    val movies = mutableListOf<Movie>()
    try {
        val result = db.collection("Movies").get().await()
        for (document in result) {
            val movie = Movie(
                title = document.getString("title"),
                director = document.getString("director"),
                actors = document.getString("actors"),
                genre = document.getString("genre"),
                releaseDate = document.getString("releaseDate"),
                duration = document.getString("duration"),
                language = document.getString("language"),
                rated = document.getString("rated"),
                details = document.getString("details"),
                trailer = document.getString("trailer"),
                imagelink = document.getString("imagelink"),
                status = document.getString("status")
            )
            movies.add(movie)
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Lỗi khi lấy dữ liệu: ${e.message}")
    }
    return movies
}