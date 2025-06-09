package com.example.movie_booking_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    private val repository = MovieRepository()

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

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

    init {
        loadMovies()
    }

    //Tải phim
    fun loadMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val allMovies = repository.getMoviesFromFirestore()
                _movies.value = allMovies
                classifyMovies(allMovies)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Hàm phân loại phim theo trường status
    private fun classifyMovies(allMovies: List<Movie>) {
        val nowPlaying = mutableListOf<Movie>()
        val upcoming = mutableListOf<Movie>()

        allMovies.forEach { movie ->
            when (movie.status) {
                "Đang chiếu" -> nowPlaying.add(movie)
                "Sắp chiếu" -> upcoming.add(movie)
                else -> nowPlaying.add(movie)
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