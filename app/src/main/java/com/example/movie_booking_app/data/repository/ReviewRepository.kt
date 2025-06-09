package com.example.movie_booking_app.data.repository

import android.util.Log
import com.example.movie_booking_app.data.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class ReviewRepository {
    private val TAG = "ReviewRepository"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val reviewsCollection = db.collection("Reviews")

//    Tạo hoặc cập nhật đánh giá của người dùng cho một bộ phim
    suspend fun submitReview(movieId: String, rating: Int, comment: String): String? {
        try {
            val userId = auth.currentUser?.uid ?: return null
            // Kiểm tra xem người dùng đã đánh giá phim này chưa
            val existingReviewQuery = reviewsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .await()
            if (!existingReviewQuery.isEmpty) {
                val existingReviewDoc = existingReviewQuery.documents[0]
                val reviewId = existingReviewDoc.id
                reviewsCollection.document(reviewId)
                    .update(
                        mapOf(
                            "rating" to rating,
                            "comment" to comment,
                            "reviewDate" to FieldValue.serverTimestamp()
                        )
                    )
                    .await()
                return reviewId
            }
            // Nếu chưa có đánh giá, tạo mới
            else {
                val reviewId = UUID.randomUUID().toString()
                val reviewData = hashMapOf(
                    "reviewId" to reviewId,
                    "movieId" to movieId,
                    "userId" to userId,
                    "rating" to rating,
                    "comment" to comment,
                    "reviewDate" to FieldValue.serverTimestamp(),
                    "helpfulVotes" to 0
                )
                reviewsCollection.document(reviewId)
                    .set(reviewData)
                    .await()
                // Cập nhật thông tin đánh giá trung bình của phim
                updateMovieRating(movieId)
                return reviewId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi đánh giá phim: ${e.message}", e)
            return null
        }
    }

//    Lấy tất cả đánh giá cho một bộ phim
    suspend fun getReviewsForMovie(movieId: String): List<Review> {
        return try {
            val reviewsCollection = FirebaseFirestore.getInstance().collection("Reviews")
            val query = reviewsCollection
                .whereEqualTo("movieId", movieId)
            val snapshot = query.get().await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    Review(
                        reviewId = doc.getString("reviewId") ?: doc.id,
                        userId = doc.getString("userId") ?: "",
                        movieId = doc.getString("movieId") ?: "",
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        comment = doc.getString("comment") ?: "",
                        reviewDate = doc.getTimestamp("reviewDate")?.toDate() ?: Date(),
                        helpfulVotes = doc.getLong("helpfulVotes")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing review document: ${e.message}")
                    null
                }
            }
            // Sắp xếp kết quả
            val sortedReviews = reviews.sortedByDescending { it.reviewDate }
            sortedReviews
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching reviews: ${e.message}", e)
            emptyList()
        }
    }

//    Lấy đánh giá của người dùng hiện tại cho một bộ phim
    suspend fun getUserReviewForMovie(movieId: String): Review? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            if (snapshot.isEmpty) return null

            val doc = snapshot.documents[0]
            val reviewId = doc.getString("reviewId") ?: doc.id
            val rating = doc.getLong("rating")?.toInt() ?: 0
            val comment = doc.getString("comment") ?: ""
            val reviewDate = doc.getTimestamp("reviewDate")?.toDate() ?: java.util.Date()
            val helpfulVotes = doc.getLong("helpfulVotes")?.toInt() ?: 0

            Review(
                reviewId = reviewId,
                movieId = movieId,
                userId = userId,
                rating = rating,
                comment = comment,
                reviewDate = reviewDate,
                helpfulVotes = helpfulVotes
            )
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy đánh giá của người dùng: ${e.message}", e)
            null
        }
    }

//    Vote đánh giá là hữu ích
    suspend fun voteReviewHelpful(reviewId: String): Boolean {
        return try {
            reviewsCollection.document(reviewId)
                .update("helpfulVotes", FieldValue.increment(1))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi vote đánh giá: ${e.message}", e)
            false
        }
    }

//    Cập nhật điểm đánh giá trung bình cho phim
    private suspend fun updateMovieRating(movieId: String) {
        try {
            val snapshot = reviewsCollection
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            var totalRating = 0
            var count = 0
            for (doc in snapshot.documents) {
                val rating = doc.getLong("rating")?.toInt() ?: continue
                totalRating += rating
                count++
            }
            if (count > 0) {
                val averageRating = totalRating.toDouble() / count
                db.collection("Movies")
                    .document(movieId)
                    .update(
                        mapOf(
                            "averageRating" to averageRating,
                            "ratingCount" to count
                        )
                    )
                    .await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi cập nhật rating phim: ${e.message}", e)
        }
    }
}