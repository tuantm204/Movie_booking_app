package com.example.movie_booking_app.data.network

import com.example.movie_booking_app.data.model.Theater
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Lấy tất cả các rạp từ Firestore
 */
suspend fun getAllTheatersFromFirestore(): List<Theater> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("Theaters")
            .get()
            .await()

        snapshot.documents.map { doc ->
            Theater(
                theaterId = doc.id,
                name = doc.getString("name") ?: "",
                location = doc.getString("location") ?: "",
                city = doc.getString("city") ?: "",
                contact = doc.getString("contact") ?: "",
                facilities = doc.get("facilities") as? List<String> ?: emptyList(),
                openTime = doc.getString("openTime") ?: "",
                closeTime = doc.getString("closeTime") ?: ""
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Lấy thông tin một rạp theo ID
 */
suspend fun getTheaterById(theaterId: String): Theater? {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val doc = firestore.collection("Theaters")
            .document(theaterId)
            .get()
            .await()

        if (doc.exists()) {
            Theater(
                theaterId = doc.id,
                name = doc.getString("name") ?: "",
                location = doc.getString("location") ?: "",
                city = doc.getString("city") ?: "",
                contact = doc.getString("contact") ?: "",
                facilities = doc.get("facilities") as? List<String> ?: emptyList(),
                openTime = doc.getString("openTime") ?: "",
                closeTime = doc.getString("closeTime") ?: ""
            )
        } else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Lấy các rạp theo thành phố
 */
suspend fun getTheatersByCity(city: String): List<Theater> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("Theaters")
            .whereEqualTo("city", city)
            .get()
            .await()

        snapshot.documents.map { doc ->
            Theater(
                theaterId = doc.id,
                name = doc.getString("name") ?: "",
                location = doc.getString("location") ?: "",
                city = doc.getString("city") ?: "",
                contact = doc.getString("contact") ?: "",
                facilities = doc.get("facilities") as? List<String> ?: emptyList(),
                openTime = doc.getString("openTime") ?: "",
                closeTime = doc.getString("closeTime") ?: ""
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}