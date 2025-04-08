package com.example.movie_booking_app.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.data.model.News
import com.example.movie_booking_app.data.network.getMoviesFromFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.tasks.await

class MovieViewModel : ViewModel() {
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies
    // StateFlow riêng cho phim đang chiếu và sắp chiếu
    private val _nowPlayingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val nowPlayingMovies: StateFlow<List<Movie>> = _nowPlayingMovies

    private val _upcomingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val upcomingMovies: StateFlow<List<Movie>> = _upcomingMovies

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedMovie = MutableStateFlow<Movie?>(null)
    val selectedMovie: StateFlow<Movie?> = _selectedMovie

    // Thêm StateFlow để quản lý dữ liệu tin tức
    private val _news = MutableStateFlow<List<News>>(emptyList())
    val news: StateFlow<List<News>> = _news.asStateFlow()

    // StateFlow để giữ tin tức được chọn
    private val _selectedNews = MutableStateFlow<News?>(null)
    val selectedNews: StateFlow<News?> = _selectedNews.asStateFlow()

    init {
        loadMovies()
        loadNews()
    }

    // Function để lấy dữ liệu tin tức từ Firebase
    fun loadNews() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val newsCollection = Firebase.firestore.collection("News")
                val result = newsCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING) // Sắp xếp theo thời gian giảm dần (mới nhất lên đầu)
                    .get()
                    .await()

                val newsList = result.documents.mapNotNull { doc ->
                    doc.toObject(News::class.java)
                }

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

    fun loadMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val allMovies = getMoviesFromFirestore()
                _movies.value = allMovies

                // Phân loại phim
                classifyMovies(allMovies)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

//    // Hàm phân loại phim thành đang chiếu và sắp chiếu
//    private fun classifyMovies(allMovies: List<Movie>) {
//        val currentDate = Date() // Ngày hiện tại
//        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Định dạng ngày trong Firestore
//
//        val nowPlaying = mutableListOf<Movie>()
//        val upcoming = mutableListOf<Movie>()
//
//        allMovies.forEach { movie ->
//            try {
//                // Chuyển đổi chuỗi ngày thành đối tượng Date
//                val releaseDate = movie.releaseDate?.let { dateFormat.parse(it) }
//
//                // Nếu ngày phát hành trước hoặc bằng ngày hiện tại -> đang chiếu
//                // Nếu sau ngày hiện tại hoặc null -> sắp chiếu
//                if (releaseDate != null && releaseDate <= currentDate) {
//                    nowPlaying.add(movie)
//                } else {
//                    upcoming.add(movie)
//                }
//            } catch (e: Exception) {
//                // Nếu có lỗi khi chuyển đổi ngày, mặc định đưa vào danh sách đang chiếu
//                nowPlaying.add(movie)
//            }
//        }
//
//        _nowPlayingMovies.value = nowPlaying
//        _upcomingMovies.value = upcoming
//    }
    // Hàm phân loại phim theo trường status
    private fun classifyMovies(allMovies: List<Movie>) {
        val nowPlaying = mutableListOf<Movie>()
        val upcoming = mutableListOf<Movie>()

        allMovies.forEach { movie ->
            when (movie.status) {
                "Đang chiếu" -> nowPlaying.add(movie)
                "Sắp chiếu" -> upcoming.add(movie)
                else -> nowPlaying.add(movie) // Mặc định thêm vào đang chiếu nếu status không rõ
            }
        }

        _nowPlayingMovies.value = nowPlaying
        _upcomingMovies.value = upcoming
    }
    fun selectMovie(movie: Movie) {
        _selectedMovie.value = movie
    }

    fun getMovieById(movieId: String): Movie? {
        return _movies.value.find { it.title == movieId }
    }

}
