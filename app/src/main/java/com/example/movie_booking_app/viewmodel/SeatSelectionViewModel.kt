package com.example.movie_booking_app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.data.model.Room
import com.example.movie_booking_app.data.model.Seat
import com.example.movie_booking_app.data.model.SeatStatus
import com.example.movie_booking_app.data.model.SeatType
import com.example.movie_booking_app.data.model.Showtime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CancellationException
import kotlin.random.Random

class SeatSelectionViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "SeatSelectionViewModel"

    private var _savedSelectedSeats = mutableListOf<Seat>()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showtime = MutableStateFlow<Showtime?>(null)
    val showtime: StateFlow<Showtime?> = _showtime.asStateFlow()

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _seatMatrix = MutableStateFlow<List<List<Seat>>>(emptyList())
    val seatMatrix: StateFlow<List<List<Seat>>> = _seatMatrix.asStateFlow()

    private val _selectedSeats = MutableStateFlow<List<Seat>>(emptyList())
    val selectedSeats: StateFlow<List<Seat>> = _selectedSeats.asStateFlow()

    private val _theaterName = MutableStateFlow<String>("")
    val theaterName: StateFlow<String> = _theaterName.asStateFlow()

    private val _theaterAddress = MutableStateFlow<String>("")
    val theaterAddress: StateFlow<String> = _theaterAddress.asStateFlow()

    private val _totalPrice = MutableStateFlow(0)
    val totalPrice: StateFlow<Int> = _totalPrice.asStateFlow()

    private val _bookingCode = MutableStateFlow("")
    val bookingCode: StateFlow<String> = _bookingCode.asStateFlow()

    private val _isBookingSaved = MutableStateFlow(false)
    val isBookingSaved: StateFlow<Boolean> = _isBookingSaved.asStateFlow()

    private val _bookingError = MutableStateFlow("")
    val bookingError: StateFlow<String> = _bookingError.asStateFlow()


    //Tải dữ liệu cho suất chiếu và hiển thị ma trận ghế
    fun loadData(showtimeId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val showtimeDoc = firestore.collection("Schedules")
                    .document(showtimeId)
                    .get(Source.SERVER)
                    .await()

                if (!showtimeDoc.exists()) {
                    Log.e(TAG, "Không tìm thấy suất chiếu: $showtimeId")
                    _seatMatrix.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                val roomId = showtimeDoc.getString("roomId") ?: ""
                val movieId = showtimeDoc.getString("movieId") ?: ""
                val price = showtimeDoc.getDouble("price") ?: 120000.0
                val startTimeStr = showtimeDoc.getString("startTime")
                val startTime = parseDate(startTimeStr) ?: Date()
                val endTimeStr = showtimeDoc.getString("endTime")
                val endTime = if (endTimeStr != null) {
                    parseDate(endTimeStr) ?: Date()
                } else {
                    val cal = java.util.Calendar.getInstance()
                    cal.time = startTime
                    cal.add(java.util.Calendar.HOUR_OF_DAY, 2)
                    cal.time
                }

                // Tạo đối tượng Showtime
                _showtime.value = Showtime(
                    scheduleId = showtimeId,
                    startTime = startTime,
                    endTime = endTime,
                    roomId = roomId,
                    roomName = "",
                    price = price,
                    availableSeats = showtimeDoc.getLong("availableSeats")?.toInt() ?: 0,
                    language = showtimeDoc.getString("language") ?: "Tiếng Việt"
                )

                // Lấy thông tin phim
                if (movieId.isNotEmpty()) {
                    loadMovieInfo(movieId)
                }

                // Lấy thông tin phòng
                val roomDoc = firestore.collection("Rooms")
                    .document(roomId)
                    .get()
                    .await()

                if (!roomDoc.exists()) {
                    _seatMatrix.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                val roomName = roomDoc.getString("name") ?: "Phòng chiếu"

                // Cập nhật tên phòng trong Showtime
                _showtime.value = _showtime.value?.copy(roomName = roomName)

                // Lưu thông tin phòng
                _room.value = Room(
                    roomId = roomId,
                    name = roomName,
                )

                // Lấy ma trận ghế từ phòng
                val seatMatrixData = roomDoc.get("seatMatrix") as? List<Map<String, Any>>

                if (seatMatrixData.isNullOrEmpty()) {
                    _seatMatrix.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }
                val bookingsQuery = firestore.collection("Bookings")
                    .whereEqualTo("scheduleId", showtimeId)
                    .whereEqualTo("status", "Đã thanh toán")
                    .get(Source.SERVER)
                    .await()

                // Danh sách ghế đã đặt
                val bookedSeatsFromBookings = mutableListOf<String>()

                // Thu thập tất cả ghế từ các booking
                for (doc in bookingsQuery.documents) {
                    val seats = doc.get("seats") as? List<*>
                    if (seats != null) {
                        seats.forEach { seat ->
                            if (seat is String) {
                                bookedSeatsFromBookings.add(seat)
                            }
                        }
                    }
                }
                // Tạo ma trận ghế sử dụng dữ liệu ghế đã đặt từ Bookings
                createSeatMatrix(seatMatrixData, bookedSeatsFromBookings)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _seatMatrix.value = emptyList()
            } finally {
                _isLoading.value = false
                // Cập nhật tổng giá sau khi tải dữ liệu
                calculateTotalPrice()
            }
        }
    }

    // Lấy thông tin phim
    private suspend fun loadMovieInfo(movieId: String) {
        try {
            val movieDoc = firestore.collection("Movies")
                .document(movieId)
                .get()
                .await()

            if (movieDoc.exists()) {
                val title = movieDoc.getString("title") ?: ""
                val imagelink = movieDoc.getString("imagelink") ?: ""
                val genre = movieDoc.getString("genre") ?: ""
                val duration = movieDoc.getString("duration") ?: ""
                val language = movieDoc.getString("language") ?: ""
                val rated = movieDoc.getString("rated") ?: "P"

                _movie.value = Movie(
                    id = movieId,
                    title = title,
                    imagelink = imagelink,
                    genre = genre,
                    duration = duration,
                    language = language,
                    rated = rated
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Lỗi khi tải thông tin phim: ${e.message}")
        }
    }

    // Hàm lưu trữ trạng thái ghế đã chọn trước khi chuyển màn hình
    fun saveSelectedSeats() {
        _savedSelectedSeats = _selectedSeats.value.toMutableList()
    }

    // Hàm khôi phục trạng thái ghế đã chọn khi quay lại màn hình
    private fun restoreSelectedSeats() {
        val savedSeatIds = _savedSelectedSeats.map { it.id }
        val currentMatrix = _seatMatrix.value.toMutableList()
        val newMatrix = currentMatrix.map { row ->
            row.map { currentSeat ->
                if (savedSeatIds.contains(currentSeat.id) && currentSeat.status == SeatStatus.AVAILABLE) {
                    currentSeat.copy(status = SeatStatus.SELECTED)
                } else {
                    currentSeat
                }
            }
        }
        _seatMatrix.value = newMatrix
        updateSelectedSeats()
    }

    //Tạo ma trận ghế dựa trên dữ liệu từ Room và danh sách ghế đã đặt
    private fun createSeatMatrix(seatMatrixData: List<Map<String, Any>>, bookedSeats: List<String>) {
        val matrix = mutableListOf<List<Seat>>()
        for (rowData in seatMatrixData) {
            val rowName = rowData["row"] as? String ?: continue
            val types = rowData["types"] as? List<String> ?: continue
            if (types.any { it.lowercase() == "double" }) {
                val doubleSeats = types.count { it.lowercase() == "double" }
                val actualDoubleSeats = doubleSeats / 2
                val row = mutableListOf<Seat>()
                var standardSeatCount = 0
                var vipSeatCount = 0
                var doubleSeatCount = 0
                for (i in types.indices) {
                    val typeStr = types[i].lowercase()
                    if (typeStr != "double") {
                        val number = (i + 1).toString()
                        val seatId = "$rowName$number"
                        val type = when (typeStr) {
                            "standard" -> {
                                standardSeatCount++
                                SeatType.STANDARD
                            }
                            "vip" -> {
                                vipSeatCount++
                                SeatType.VIP
                            }
                            else -> {
                                standardSeatCount++
                                SeatType.STANDARD
                            }
                        }
                        val status = if (bookedSeats.contains(seatId))
                            SeatStatus.BOOKED else SeatStatus.AVAILABLE
                        row.add(Seat(id = seatId, row = rowName, number = number, type = type, status = status))
                    }
                }

                // Thêm các ghế Double (đã được tính toán lại)
                for (i in 0 until actualDoubleSeats) {
                    val doubleIndex = i * 2
                    val number1 = types.indexOfFirst { it.lowercase() == "double" } + doubleIndex + 1
                    val number2 = number1 + 1
                    val seatId = "$rowName$number1-$number2"

                    // Kiểm tra xem có ghế nào trong cặp đã được đặt chưa
                    val isBooked = bookedSeats.contains("$rowName$number1") ||
                            bookedSeats.contains("$rowName$number2") ||
                            bookedSeats.contains(seatId)
                    val status = if (isBooked) SeatStatus.BOOKED else SeatStatus.AVAILABLE
                    row.add(Seat(
                        id = seatId,
                        row = rowName,
                        number = "$number1-$number2",
                        type = SeatType.DOUBLE,
                        status = status
                    ))
                    doubleSeatCount++
                }

                // Sắp xếp lại ghế theo vị trí số
                val sortedRow = row.sortedBy {
                    val numberParts = it.number.split("-")
                    numberParts.first().toIntOrNull() ?: 0
                }

                if (sortedRow.isNotEmpty()) {
                    matrix.add(sortedRow)
                }
            } else {
                // Xử lý bình thường cho hàng không có ghế Double
                val row = mutableListOf<Seat>()
                for (i in types.indices) {
                    val number = (i + 1).toString()
                    val seatId = "$rowName$number"

                    val typeStr = types[i].lowercase()
                    val type = when (typeStr) {
                        "standard" -> SeatType.STANDARD
                        "vip" -> SeatType.VIP
                        "sweetbox" -> SeatType.STANDARD
                        else -> SeatType.STANDARD
                    }
                    val status = if (bookedSeats.contains(seatId))
                        SeatStatus.BOOKED else SeatStatus.AVAILABLE
                    row.add(Seat(id = seatId, row = rowName, number = number, type = type, status = status))
                }
                if (row.isNotEmpty()) {
                    matrix.add(row)
                }
            }
        }
        _seatMatrix.value = matrix
    }

    // Chọn ghế
    fun toggleSeatSelection(seat: Seat) {
        if (seat.status == SeatStatus.BOOKED || seat.status == SeatStatus.UNAVAILABLE) {
            return
        }
        val currentMatrix = _seatMatrix.value.toMutableList()
        val newMatrix = currentMatrix.map { row ->
            row.map { currentSeat ->
                if (currentSeat.id == seat.id) {
                    val newStatus = if (currentSeat.status == SeatStatus.SELECTED)
                        SeatStatus.AVAILABLE else SeatStatus.SELECTED
                    currentSeat.copy(status = newStatus)
                } else {
                    currentSeat
                }
            }
        }
        _seatMatrix.value = newMatrix
        updateSelectedSeats()
    }

    //Cập nhật danh sách ghế đã chọn dựa trên ma trận ghế hiện tại
    fun updateSelectedSeats() {
        val selectedSeats = _seatMatrix.value.flatten()
            .filter { it.status == SeatStatus.SELECTED }
        _selectedSeats.value = selectedSeats
        calculateTotalPrice()
    }
    //Tính toán tổng giá tiền của các ghế đã chọn
    fun calculateTotalPrice() {
        val basePrice = _showtime.value?.price ?: 120000.0
        var total = 0.0

        for (seat in _selectedSeats.value) {
            val multiplier = when (seat.type) {
                SeatType.STANDARD -> 1.0
                SeatType.VIP -> 1.3
                SeatType.DOUBLE -> 2.0
            }
            total += basePrice * multiplier
        }
        _totalPrice.value = total.toInt()
    }

    //Chuyển đổi chuỗi ngày tháng thành đối tượng Date
    private fun parseDate(dateStr: String?): Date? {
        if (dateStr == null) return null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).parse(dateStr)
            } catch (e: Exception) {
            }
        }
        return null
    }

    //Cập nhật thông tin rạp chiếu phim
    fun setTheaterInfo(name: String, address: String) {
        _theaterName.value = name
        _theaterAddress.value = address
    }

    //Cập nhật trực tiếp tổng giá tiền
    fun setTotalPrice(price: Int) {
        _totalPrice.value = price
    }

    //Tạo mã đặt vé ngẫu nhiên
    fun generateBookingCode() {
        _bookingCode.value = "MBA" + Random.nextInt(10000000, 99999999).toString()
    }

    //Lưu thông tin booking vào Firestore
    fun saveBookingToFirestore() {
        if (_bookingCode.value.isEmpty()) {
            generateBookingCode()
        }
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _bookingError.value = "Bạn cần đăng nhập để đặt vé"
                    return@launch
                }
                val userId = currentUser.uid
                val currentShowtime = showtime.value
                val selectedSeatIds = selectedSeats.value.map { it.id }
                val currentMovie = movie.value

                if (currentShowtime == null || currentMovie == null) {
                    _bookingError.value = "Thiếu thông tin đặt vé"
                    return@launch
                }

                // Kiểm tra lại xem ghế đã được đặt chưa
                val latestBookings = firestore.collection("Bookings")
                    .whereEqualTo("scheduleId", currentShowtime.scheduleId)
                    .whereEqualTo("status", "Đã thanh toán")
                    .get(Source.SERVER)
                    .await()

                val latestBookedSeats = mutableSetOf<String>()
                latestBookings.documents.forEach { doc ->
                    (doc.get("seats") as? List<*>)?.forEach { seat ->
                        if (seat is String) latestBookedSeats.add(seat)
                    }
                }

                val conflictSeats = selectedSeatIds.filter { latestBookedSeats.contains(it) }
                if (conflictSeats.isNotEmpty()) {
                    _bookingError.value = "Ghế ${conflictSeats.joinToString(", ")} đã được đặt. Vui lòng chọn ghế khác."
                    return@launch
                }

                // Tạo booking object với tổng tiền được truyền từ OrderSummaryScreen
                val booking = hashMapOf(
                    "bookingId" to _bookingCode.value,
                    "userId" to userId,
                    "scheduleId" to currentShowtime.scheduleId,
                    "movieId" to currentMovie.id,
                    "roomId" to currentShowtime.roomId,
                    "seats" to selectedSeatIds,
                    "totalPrice" to _totalPrice.value,
                    "bookingTime" to FieldValue.serverTimestamp(),
                    "status" to "Đã thanh toán"
                )

                val db = FirebaseFirestore.getInstance()
                db.collection("Bookings")
                    .document(_bookingCode.value)
                    .set(booking)
                    .await()

                _isBookingSaved.value = true
                _bookingError.value = ""
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }
}