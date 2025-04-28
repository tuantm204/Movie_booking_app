package com.example.movie_booking_app.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.model.News
import com.example.movie_booking_app.data.network.getNewsFromFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    // StateFlow để quản lý dữ liệu tin tức
    private val _news = MutableStateFlow<List<News>>(emptyList())
    val news: StateFlow<List<News>> = _news.asStateFlow()

    // StateFlow để giữ tin tức được chọn
    private val _selectedNews = MutableStateFlow<News?>(null)
    val selectedNews: StateFlow<News?> = _selectedNews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadNews()
    }

    // Function để lấy dữ liệu tin tức từ Firebase
    fun loadNews() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val newsList = getNewsFromFirestore()
                _news.value = newsList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải tin tức: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function để chọn tin tức
    fun selectNews(news: News) {
        _selectedNews.value = news
    }
}