package com.example.movie_booking_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.model.Booking
import com.example.movie_booking_app.data.model.Theater
import com.example.movie_booking_app.data.model.TheaterWithShowtimes
import com.example.movie_booking_app.data.repository.BookingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookingViewModel : ViewModel() {
    private val TAG = "BookingViewModel"
    private val repository = BookingRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _currentMovieId = MutableStateFlow<String>("")

    private val _availableDates = MutableStateFlow<List<Date>>(emptyList())
    val availableDates: StateFlow<List<Date>> = _availableDates.asStateFlow()

    private val _selectedDate = MutableStateFlow<Date>(Calendar.getInstance().time)
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _theaters = MutableStateFlow<List<TheaterWithShowtimes>>(emptyList())
    val theaters: StateFlow<List<TheaterWithShowtimes>> = _theaters.asStateFlow()

    private val _expandedTheaterId = MutableStateFlow<String?>(null)
    val expandedTheaterId: StateFlow<String?> = _expandedTheaterId.asStateFlow()

    private val _userBookings = MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> = _userBookings.asStateFlow()

    private val _upcomingBookings = MutableStateFlow<List<Booking>>(emptyList())
    val upcomingBookings: StateFlow<List<Booking>> = _upcomingBookings.asStateFlow()

    private val _pastBookings = MutableStateFlow<List<Booking>>(emptyList())
    val pastBookings: StateFlow<List<Booking>> = _pastBookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi"))

//    Tải dữ liệu cho phim và ưu tiên chọn ngày hiện tại
    fun loadDataForMovie(movieId: String) {
        if (movieId.isBlank()) {
            return
        }
        _currentMovieId.value = movieId
        viewModelScope.launch {
            _isLoading.value = true
            // Lấy các ngày có sẵn từ hôm nay trở đi
            repository.getAvailableDates(movieId).collectLatest { dates ->
                if (dates.isNotEmpty()) {
                    _availableDates.value = dates
                    val today = Calendar.getInstance().time
                    val todayDate = dates.find { isSameDay(it, today) }
                    val dateToSelect = todayDate ?: dates.first()
                    _selectedDate.value = dateToSelect
                    loadTheatersForDate(movieId, dateToSelect)
                } else {
                    _availableDates.value = emptyList()
                    _theaters.value = emptyList()
                    _error.value = "Không có lịch chiếu nào từ hôm nay trở đi cho phim này"
                }
                _isLoading.value = false
            }
        }
    }

//    Kiểm tra hai ngày có cùng ngày không
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

//    CHọn ngày
    fun selectDate(date: Date) {
        val movieId = _currentMovieId.value
        if (movieId.isBlank()) {
            return
        }
        viewModelScope.launch {
            _selectedDate.value = date
            loadTheatersForDate(movieId, date)
        }
    }

//    Tải danh sách rạp chiếu cho ngày đã chọn
    private fun loadTheatersForDate(movieId: String, date: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale("vi")).format(date)
            val timeStr = SimpleDateFormat("HH:mm", Locale("vi")).format(Date())
            Log.d(TAG, "Đang tải rạp cho phim $movieId vào ngày $dateStr (giờ hiện tại: $timeStr)")
            try {
                repository.getTheatersWithShowtimes(movieId, date).collectLatest { theaterList ->
                    _theaters.value = theaterList
                    _isLoading.value = false

                    if (theaterList.isEmpty()) {
                        val today = Calendar.getInstance().time
                        if (isSameDay(date, today)) {
                            _error.value = "Không còn suất chiếu nào sau giờ hiện tại cho ngày hôm nay"
                        } else {
                            _error.value = "Không có suất chiếu nào cho ngày đã chọn"
                        }
                    } else {
                        _error.value = null
                        Log.d(TAG, "Đã tải ${theaterList.size} rạp có suất chiếu")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi tải danh sách rạp: ${e.message}", e)
                _theaters.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    //Quản lý trạng thái mở rộng/thu gọn của các rạp
    fun toggleTheaterExpansion(theaterId: String) {
        _expandedTheaterId.value = if (_expandedTheaterId.value == theaterId) null else theaterId
    }

    //Láy vé của user
    fun getUserBookings() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _error.value = "Bạn cần đăng nhập để xem vé"
            return
        }
        val userId = currentUser.uid
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val bookingsSnapshot = db.collection("Bookings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val bookingsList = mutableListOf<Booking>()
                val upcomingList = mutableListOf<Booking>()
                val pastList = mutableListOf<Booking>()
                val now = Date()

                for (document in bookingsSnapshot.documents) {
                    try {
                        val bookingId = document.getString("bookingId") ?: document.id
                        val movieId = document.getString("movieId") ?: ""
                        val scheduleId = document.getString("scheduleId") ?: ""
                        val roomId = document.getString("roomId") ?: ""
                        val status = document.getString("status") ?: "Đã thanh toán"
                        val totalPrice = document.getLong("totalPrice")?.toInt() ?: 0
                        val bookingTime = document.getTimestamp("bookingTime")?.toDate() ?: Date()
                        val seats = when (val seatsData = document.get("seats")) {
                            is List<*> -> seatsData.mapNotNull { it?.toString() }
                            else -> emptyList()
                        }
                        // Lấy thông tin Movie
                        var movieTitle = "Unknown Movie"
                        var movieImageUrl = ""
                        if (movieId.isNotEmpty()) {
                            try {
                                val movieDoc = db.collection("Movies").document(movieId).get().await()
                                movieTitle = movieDoc.getString("title") ?: "Unknown Movie"
                                movieImageUrl = movieDoc.getString("imagelink") ?: ""
                            } catch (e: Exception) {
                                Log.e(TAG, "Error fetching movie: $e")
                            }
                        }

                        // Lấy thông tin Schedule
                        var showDate = ""
                        var showTime = ""
                        var showDateTime: Date? = null

                        if (scheduleId.isNotEmpty()) {
                            try {
                                val scheduleDoc = db.collection("Schedules").document(scheduleId).get().await()
                                val startTimeStr = scheduleDoc.getString("startTime") ?: ""

                                if (startTimeStr.isNotEmpty()) {
                                    try {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                        showDateTime = sdf.parse(startTimeStr)

                                        if (showDateTime != null) {
                                            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
                                            val timeFormatter = SimpleDateFormat("HH:mm", Locale("vi"))
                                            showDate = dateFormatter.format(showDateTime)
                                            showTime = timeFormatter.format(showDateTime)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error parsing schedule time: $e")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error fetching schedule: $e")
                            }
                        }

                        // Lấy thông tin Room và Theater
                        var roomName = "Unknown Room"
                        var theaterName = ""
                        var theaterLocation = ""
                        var screenType = ""
                        
                        if (roomId.isNotEmpty()) {
                            try {
                                val roomDoc = db.collection("Rooms").document(roomId).get().await()
                                roomName = roomDoc.getString("name") ?: roomId
                                screenType = roomDoc.getString("screenType") ?: ""
                                val theaterId = roomDoc.getString("theaterId")
                                if (theaterId != null) {
                                    val theaterDoc = db.collection("Theaters").document(theaterId).get().await()
                                    if (theaterDoc.exists()) {
                                        theaterName = theaterDoc.getString("name") ?: ""
                                        theaterLocation = theaterDoc.getString("location") ?: ""
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error fetching room and theater: $e")
                            }
                        }
                        // Tạo object Booking với thông tin đầy đủ
                        val booking = Booking(
                            id = bookingId,
                            userId = userId,
                            movieId = movieId,
                            movieTitle = movieTitle,
                            movieImageUrl = movieImageUrl,
                            scheduleId = scheduleId,
                            showDate = showDate,
                            showTime = showTime,
                            roomId = roomId,
                            roomName = roomName,
                            seats = seats,
                            totalPrice = totalPrice,
                            bookingTime = bookingTime,
                            status = status,
                            theaterName = theaterName,
                            theaterLocation = theaterLocation,
                            screenType = screenType
                        )
                        bookingsList.add(booking)

                        // Phân loại theo thời gian chiếu
                        if (showDateTime != null && showDateTime.after(now)) {
                            upcomingList.add(booking)
                        } else {
                            pastList.add(booking)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing booking document: $e")
                    }
                }
                // Sắp xếp theo thời gian đặt vé
                val sortedBookings = bookingsList.sortedByDescending { it.bookingTime }
                val sortedUpcoming = upcomingList.sortedByDescending { it.bookingTime }
                val sortedPast = pastList.sortedByDescending { it.bookingTime }

                // Cập nhật state
                _userBookings.value = sortedBookings
                _upcomingBookings.value = sortedUpcoming
                _pastBookings.value = sortedPast
            } catch (e: Exception) {
                _error.value = e.message ?: "Đã xảy ra lỗi khi tải danh sách vé"
            } finally {
                _isLoading.value = false
            }
        }
    }

    //Tìm kiếm một booking
    fun getBookingById(bookingId: String): Booking? {
        if (bookingId.isEmpty()) {
            return null
        }
        val allBookings = _userBookings.value + _upcomingBookings.value + _pastBookings.value
        val booking = allBookings.find {
            val match = it.id == bookingId
            match
        }
        return booking
    }
}