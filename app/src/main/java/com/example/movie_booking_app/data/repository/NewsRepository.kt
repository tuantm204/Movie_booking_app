package com.example.movie_booking_app.data.repository

import android.util.Log
import com.example.movie_booking_app.data.model.News
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NewsRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getNewsFromFirestore(): List<News> {
        val newsList = mutableListOf<News>()
        try {
            // Thêm orderBy để sắp xếp theo thời gian mới nhất
            val result = firestore.collection("News")
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
                newsList.add(news)
            }
        } catch (e: Exception) {
            Log.e("NewsRepository", "Lỗi khi lấy dữ liệu tin tức: ${e.message}")
        }
        return newsList
    }
}