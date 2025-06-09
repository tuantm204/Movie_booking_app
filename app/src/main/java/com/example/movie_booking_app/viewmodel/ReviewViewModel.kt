package com.example.movie_booking_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.model.Review
import com.example.movie_booking_app.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {
    private val TAG = "ReviewViewModel"
    private val repository = ReviewRepository()

    // State cho danh sách đánh giá
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _userReview = MutableStateFlow<Review?>(null)
    val userReview: StateFlow<Review?> = _userReview.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    private val _ratedMovieIds = MutableStateFlow<Set<String>>(emptySet())
    val ratedMovieIds: StateFlow<Set<String>> = _ratedMovieIds.asStateFlow()

    private val _averageRating = MutableStateFlow(0.0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    private val _ratingsDistribution = MutableStateFlow(mapOf(
        5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0
    ))
    val ratingsDistribution: StateFlow<Map<Int, Int>> = _ratingsDistribution.asStateFlow()

    private val _filterRating = MutableStateFlow(0)
    val filterRating: StateFlow<Int> = _filterRating.asStateFlow()

    private val _filteredReviews = MutableStateFlow<List<Review>>(emptyList())
    val filteredReviews: StateFlow<List<Review>> = _filteredReviews.asStateFlow()

    //Lấy đánh giá
    fun getReviewsForMovie(movieId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val reviewsList = repository.getReviewsForMovie(movieId)
                _reviews.value = reviewsList

                calculateAverageRating(reviewsList)
                calculateRatingsDistribution(reviewsList)

                applyFilter(_filterRating.value)
            } catch (e: Exception) {
                _error.value = "Không thể tải đánh giá: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    //Tính điểm trung bình của phim
    private fun calculateAverageRating(reviews: List<Review>) {
        if (reviews.isEmpty()) {
            _averageRating.value = 0.0f
            return
        }

        val sum = reviews.sumOf { it.rating }
        _averageRating.value = (sum.toFloat() / reviews.size)
    }

    //Tính phân bố số lượng đánh giá theo sao
    private fun calculateRatingsDistribution(reviews: List<Review>) {
        val distribution = mutableMapOf(5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0)

        reviews.forEach { review ->
            val count = distribution[review.rating] ?: 0
            distribution[review.rating] = count + 1
        }

        _ratingsDistribution.value = distribution
    }

    //Áp dụng bộ lọc đánh giá theo số sao
    fun applyFilter(rating: Int) {
        _filterRating.value = rating
        val filtered = if (rating == 0) {
            _reviews.value
        } else {
            _reviews.value.filter { it.rating == rating }
        }
        _filteredReviews.value = filtered
    }

    //Lấy đánh giá của người dùng hiện tại cho một phim
    fun getUserReviewForMovie(movieId: String) {
        viewModelScope.launch {
            try {
                val review = repository.getUserReviewForMovie(movieId)
                _userReview.value = review
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi lấy đánh giá người dùng: ${e.message}", e)
            }
        }
    }

    //Gửi đánh giá phim mới hoặc cập nhật đánh giá cũ
    fun submitReview(movieId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _submitSuccess.value = false
            _error.value = null
            try {
                val reviewId = repository.submitReview(movieId, rating, comment)
                if (reviewId != null) {
                    _submitSuccess.value = true
                    _ratedMovieIds.update { currentSet ->
                        currentSet + movieId
                    }
                    getUserReviewForMovie(movieId)
                } else {
                    _error.value = "Không thể gửi đánh giá, vui lòng thử lại"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // Kiểm tra xem phim đã được đánh giá chưa
    fun checkIfMovieRated(movieId: String) {
        viewModelScope.launch {
            try {
                val review = repository.getUserReviewForMovie(movieId)
                if (review != null) {
                    _ratedMovieIds.update { currentSet ->
                        currentSet + movieId
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi kiểm tra đánh giá: ${e.message}", e)
            }
        }
    }

    //Vote đánh giá là hữu ích
    fun voteReviewHelpful(reviewId: String) {
        viewModelScope.launch {
            try {
                val success = repository.voteReviewHelpful(reviewId)
                if (success) {
                    val currentReviews = _reviews.value.toMutableList()
                    val reviewIndex = currentReviews.indexOfFirst { it.reviewId == reviewId }

                    if (reviewIndex != -1) {
                        val reviewToUpdate = currentReviews[reviewIndex]
                        val updatedReview = reviewToUpdate.copy(helpfulVotes = reviewToUpdate.helpfulVotes + 1)
                        currentReviews[reviewIndex] = updatedReview
                        _reviews.value = currentReviews
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi vote đánh giá: ${e.message}", e)
            }
        }
    }
    fun clearSubmitStatus() {
        _submitSuccess.value = false
    }
}