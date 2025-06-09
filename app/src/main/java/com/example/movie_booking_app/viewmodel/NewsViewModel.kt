package com.example.movie_booking_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.model.News
import com.example.movie_booking_app.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val repository = NewsRepository()

    private val _news = MutableStateFlow<List<News>>(emptyList())
    val news: StateFlow<List<News>> = _news.asStateFlow()

    private val _selectedNews = MutableStateFlow<News?>(null)
    val selectedNews: StateFlow<News?> = _selectedNews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadNews()
    }

    //Tải Tin tức
    fun loadNews() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val newsList = repository.getNewsFromFirestore()
                _news.value = newsList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải tin tức: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    //Chọn tin tức
    fun selectNews(news: News) {
        _selectedNews.value = news
    }
}