package com.example.movie_booking_app.data.repository

import android.util.Log
import com.example.movie_booking_app.data.model.Theater
import com.example.movie_booking_app.data.model.Room
import com.example.movie_booking_app.data.model.TheaterWithShowtimes
import com.example.movie_booking_app.data.model.Showtime
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "BookingRepository"

class BookingRepository {

//    Lấy danh sách các ngày có lịch chiếu cho một phim từ ngày hôm nay trở đi
    fun getAvailableDates(movieId: String): Flow<List<Date>> = flow {
        try {
            val allDates = getAvailableDatesForMovie(movieId)
            val today = resetTimeToMidnight(Calendar.getInstance().time)
            val futureDates = allDates.filter { !resetTimeToMidnight(it).before(today) }
                                    .sortedBy { it.time }
            if (futureDates.isNotEmpty()) {
                emit(futureDates)
            }
        } catch (e: Exception) {
            // Xử lý lỗi và trả về danh sách mẫu
            val calendar = Calendar.getInstance()
            val tempDates = mutableListOf<Date>()
            for (i in 0..6) {
                tempDates.add(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            emit(tempDates)
        }
    }

//    Đặt giờ, phút, giây về 0 để so sánh ngày
    private fun resetTimeToMidnight(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

//    Lấy danh sách rạp và suất chiếu cho một phim vào một ngày cụ thể
    fun getTheatersWithShowtimes(movieId: String, selectedDate: Date): Flow<List<TheaterWithShowtimes>> = flow {
        try {
            // Lấy dữ liệu từ Firestore
            val theaterShowtimes = getTheaterShowtimesByMovieAndDate(movieId, selectedDate)
            val result = mutableListOf<TheaterWithShowtimes>()

            // Lấy thông tin đầy đủ cho mỗi rạp
            for ((theaterId, showtimes) in theaterShowtimes) {
                val theater = getTheaterById(theaterId)

                if (theater != null && showtimes.isNotEmpty()) {
                    result.add(
                        TheaterWithShowtimes(
                            theaterId = theaterId,
                            name = theater.name,
                            location = theater.location,
                            city = theater.city,
                            showtimes = showtimes.sortedBy { showtime -> showtime.startTime }
                        )
                    )
                }
            }

            if (result.isEmpty()) {
                emit(createDemoTheaters(selectedDate))
            } else {
                emit(result)
            }
        } catch (e: Exception) {
            emit(createDemoTheaters(selectedDate))
        }
    }

//    Tạo dữ liệu rạp và suất chiếu mẫu
    private fun createDemoTheaters(selectedDate: Date): List<TheaterWithShowtimes> {
        return listOf(
        )
    }

//    Lấy tất cả ngày có suất chiếu cho một phim
    private suspend fun getAvailableDatesForMovie(movieId: String): List<Date> {
        val firestore = FirebaseFirestore.getInstance()

        return try {
            val snapshot = firestore.collection("Schedules")
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            val dateSet = mutableSetOf<String>()
            val result = mutableListOf<Date>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (doc in snapshot.documents) {
                try {
                    // Thử đọc startTime từ String
                    val startTimeStr = doc.getString("startTime")
                    if (startTimeStr != null) {
                        val date = parseDate(startTimeStr)
                        if (date != null) {
                            val dateStr = dateFormat.format(date)
                            if (!dateSet.contains(dateStr)) {
                                dateSet.add(dateStr)
                                val cal = Calendar.getInstance()
                                cal.time = date
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)

                                result.add(cal.time)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Lỗi xử lý ngày: ${e.message}")
                }
            }
            result.sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy ngày chiếu: ${e.message}")
            emptyList()
        }
    }

//    Lấy tất cả suất chiếu theo rạp chiếu và ngày
    private suspend fun getTheaterShowtimesByMovieAndDate(movieId: String, date: Date): Map<String, List<Showtime>> {
        val schedules = getSchedulesByMovieAndDate(movieId, date)
        if (schedules.isEmpty()) {
            return emptyMap()
        }
        val roomIds = schedules.map { it.roomId }.distinct()
        val roomToTheaterId = mutableMapOf<String, String>()

        // Lấy theaterId cho mỗi room
        for (roomId in roomIds) {
            val room = getRoomById(roomId)
            if (room != null) {
                roomToTheaterId[roomId] = room.theaterId
            }
        }
        // Nhóm lịch chiếu theo theaterId
        return schedules
            .filter { roomToTheaterId.containsKey(it.roomId) }
            .groupBy { roomToTheaterId[it.roomId]!! }
    }

    //Lấy tất cả suất chiếu cho một phim vào một ngày cụ thể
    // Chỉ lấy các suất chiếu bắt đầu sau giờ hiện tại
    private suspend fun getSchedulesByMovieAndDate(movieId: String, date: Date): List<Showtime> {
        val firestore = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = dateFormat.format(date)
        
        // Lấy thời gian hiện tại
        val currentTime = Date()
        val isToday = dateFormat.format(currentTime) == selectedDateStr

        return try {
            val snapshot = firestore.collection("Schedules")
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            val result = mutableListOf<Showtime>()

            for (doc in snapshot.documents) {
                try {
                    // Đọc startTime từ String
                    val startTimeStr = doc.getString("startTime")
                    if (startTimeStr != null) {
                        val startTime = parseDate(startTimeStr)
                        if (startTime != null) {
                            // Kiểm tra ngày
                            val startDateStr = dateFormat.format(startTime)
                            if (startDateStr == selectedDateStr) {
                                // Nếu là ngày hôm nay, chỉ lấy các suất chiếu sau giờ hiện tại
                                // Nếu là ngày khác, lấy tất cả các suất
                                if (!isToday || startTime.after(currentTime)) {
                                    val roomId = doc.getString("roomId") ?: continue
                                    val room = getRoomById(roomId) ?: continue

                                    val endTimeStr = doc.getString("endTime")
                                    val endTime = if (endTimeStr != null) {
                                        parseDate(endTimeStr)
                                    } else {
                                        val cal = Calendar.getInstance()
                                        cal.time = startTime
                                        cal.add(Calendar.HOUR_OF_DAY, 2)
                                        cal.time
                                    }

                                    result.add(
                                        Showtime(
                                            scheduleId = doc.id,
                                            startTime = startTime,
                                            endTime = endTime ?: startTime,
                                            roomId = roomId,
                                            roomName = room.name,
                                            price = doc.getDouble("price") ?: 0.0,
                                            availableSeats = doc.getLong("availableSeats")?.toInt() ?: 0,
                                            language = doc.getString("language") ?: "Tiếng Việt"
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Lỗi xử lý schedule: ${e.message}")
                }
            }
            result.sortedBy { it.startTime }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy lịch chiếu: ${e.message}")
            emptyList()
        }
    }

//    Phân tích chuỗi thời gian thành Date
    private fun parseDate(dateStr: String): Date? {
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

//    Lấy thông tin rạp theo ID
    private suspend fun getTheaterById(theaterId: String): Theater? {
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

//    Lấy thông tin phòng theo ID
    private suspend fun getRoomById(roomId: String): Room? {
        val firestore = FirebaseFirestore.getInstance()
        return try {
            val doc = firestore.collection("Rooms")
                .document(roomId)
                .get()
                .await()

            if (doc.exists()) {
                Room(
                    roomId = doc.id,
                    theaterId = doc.getString("theaterId") ?: "",
                    name = doc.getString("name") ?: "",
                    seatingCapacity = doc.getLong("seatingCapacity")?.toInt() ?: 0,
                    screenType = doc.getString("screenType") ?: "",
                    availableSeats = doc.getLong("availableSeats")?.toInt() ?: 0,
                    facilities = doc.get("facilities") as? List<String> ?: emptyList(),
                    seatMatrix = emptyList()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}