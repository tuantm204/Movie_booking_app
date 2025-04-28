package com.example.movie_booking_app.data.network
import android.util.Log
import com.example.movie_booking_app.data.model.News
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

suspend fun getNewsFromFirestore(): List<News> {
    val db = FirebaseFirestore.getInstance()
    val newslist = mutableListOf<News>()
    try {
        // Thêm orderBy để sắp xếp theo thời gian mới nhất
        val result = db.collection("News")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        for (document in result) {
            val news = News(
                id = document.id,
                title = document.getString("title") ?: "",
                time = document.getString("time") ?: "",
                timestamp = document.getLong("timestamp") ?: 0L,
                image = document.getString("image") ?: "",
                bannerImage = document.getString("bannerImage") ?: "",
                content = document.getString("content") ?: "",
                category = document.getString("category") ?: "",
                isPromoted = document.getBoolean("isPromoted") ?: false
            )
            newslist.add(news)
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Lỗi khi lấy dữ liệu: ${e.message}")
    }
    return newslist
}