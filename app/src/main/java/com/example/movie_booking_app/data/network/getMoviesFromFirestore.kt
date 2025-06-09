package com.example.movie_booking_app.data.network
import android.util.Log
import com.example.movie_booking_app.data.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Hoặc nơi bạn lấy dữ liệu phim từ Firestore

suspend fun getMoviesFromFirestore(): List<Movie> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("Movies")
            .get()
            .await()

        snapshot.documents.map { doc ->
            Movie(
                id = doc.id, // Sử dụng ID của document làm movie ID
                title = doc.getString("title") ?: "",
                imagelink = doc.getString("imagelink") ?: "",
                genre = doc.getString("genre") ?: "",
                duration = doc.getString("duration") ?: "",
                rated = doc.getString("rated") ?: "",
                status = doc.getString("status") ?: "",
                releaseDate = doc.getString("releaseDate") ?: "",
                director = doc.getString("director") ?: "",
                actors = doc.getString("actors") ?: "",
                language = doc.getString("language") ?: "",
                details = doc.getString("details") ?: "",
                trailer = doc.getString("trailer") ?: ""
            )
        }
    } catch (e: Exception) {
        Log.e("MovieRepository", "Lỗi khi lấy phim: ${e.message}")
        emptyList()
    }
}