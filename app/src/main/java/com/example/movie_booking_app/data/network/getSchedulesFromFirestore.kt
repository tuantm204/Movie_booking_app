package com.example.movie_booking_app.data.network

import android.util.Log
import com.example.movie_booking_app.data.model.Showtime
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "SchedulesFirestore"

/**
 * Lấy tất cả suất chiếu cho một phim vào một ngày cụ thể
 */
suspend fun getSchedulesByMovieAndDate(movieId: String, date: Date): List<Showtime> {
    val firestore = FirebaseFirestore.getInstance()

    // Format ngày để logging
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    Log.d(TAG, "Tìm suất chiếu cho phim $movieId vào ngày ${dateFormat.format(date)}")

    // Tạo timestamp cho đầu ngày và cuối ngày
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startOfDay = dateFormat.format(calendar.time)

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    val endOfDay = dateFormat.format(calendar.time)

    Log.d(TAG, "Khoảng thời gian tìm kiếm: $startOfDay đến $endOfDay")

    return try {
        val schedules = mutableListOf<Showtime>()

        // Truy vấn suất chiếu theo movieId
        val snapshot = firestore.collection("Schedules")
            .whereEqualTo("movieId", movieId)
            .get()
            .await()

        Log.d(TAG, "Tìm thấy ${snapshot.size()} suất chiếu tổng cộng cho phim $movieId")

        for (doc in snapshot.documents) {
            Log.d(TAG, "Kiểm tra lịch chiếu ${doc.id}")

            try {
                // Lấy startTime an toàn - xử lý cả String và Timestamp
                val startTime = getDateSafely(doc, "startTime")
                if (startTime == null) {
                    Log.d(TAG, "Bỏ qua suất ${doc.id}: không có startTime hợp lệ")
                    continue
                }

                // Kiểm tra xem suất chiếu có nằm trong ngày đã chọn không
                val startDateStr = dateFormat.format(startTime)
                val selectedDateStr = dateFormat.format(date)

                if (startDateStr != selectedDateStr) {
                    Log.d(TAG, "Bỏ qua suất ${doc.id}: không nằm trong ngày đã chọn ($startDateStr ≠ $selectedDateStr)")
                    continue
                }

                // Lấy các thông tin khác
                val roomId = doc.getString("roomId")
                if (roomId == null) {
                    Log.d(TAG, "Bỏ qua suất ${doc.id}: không có roomId")
                    continue
                }

                // Lấy thông tin phòng
                val room = getRoomById(roomId)
                if (room == null) {
                    Log.d(TAG, "Bỏ qua suất ${doc.id}: không tìm thấy thông tin phòng $roomId")
                    continue
                }

                // Lấy endTime
                val endTime = getDateSafely(doc, "endTime") ?: run {
                    // Nếu không có endTime, tạo giá trị mặc định = startTime + 2 giờ
                    val cal = Calendar.getInstance()
                    cal.time = startTime
                    cal.add(Calendar.HOUR_OF_DAY, 2)
                    cal.time
                }

                // Tạo đối tượng Showtime
                schedules.add(
                    Showtime(
                        scheduleId = doc.id,
                        startTime = startTime,
                        endTime = endTime,
                        roomId = roomId,
                        roomName = room.name,
                        price = doc.getDouble("price") ?: 0.0,
                        availableSeats = doc.getLong("availableSeats")?.toInt() ?: 0,
                        language = doc.getString("language") ?: "Tiếng Việt"
                    )
                )

                Log.d(TAG, "Đã thêm suất chiếu ${doc.id} vào lúc ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(startTime)}")

            } catch (e: Exception) {
                Log.e(TAG, "Lỗi xử lý suất chiếu ${doc.id}: ${e.message}")
            }
        }

        Log.d(TAG, "Tìm thấy ${schedules.size} suất chiếu hợp lệ cho ngày đã chọn")
        schedules.sortedBy { it.startTime }
    } catch (e: Exception) {
        Log.e(TAG, "Lỗi khi truy vấn suất chiếu: ${e.message}", e)
        emptyList()
    }
}

/**
 * Hàm trợ giúp để lấy trường dạng Date từ Firestore một cách an toàn
 * Xử lý nhiều kiểu dữ liệu khác nhau (Timestamp, String...)
 */
private fun getDateSafely(doc: com.google.firebase.firestore.DocumentSnapshot, fieldName: String): Date? {
    return try {
        // Thử đọc dưới dạng Timestamp
        doc.getTimestamp(fieldName)?.toDate()
    } catch (e: Exception) {
        try {
            // Nếu không phải Timestamp, thử đọc dưới dạng String
            val dateStr = doc.getString(fieldName)
            if (dateStr.isNullOrEmpty()) {
                Log.d(TAG, "Trường $fieldName trống hoặc null")
                null
            } else {
                Log.d(TAG, "Đang parse $fieldName từ String: $dateStr")
                parseStringToDate(dateStr)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Không thể lấy $fieldName: ${e.message}")
            null
        }
    }
}

/**
 * Chuyển chuỗi thời gian thành Date
 * Hỗ trợ nhiều định dạng khác nhau
 */
private fun parseStringToDate(dateStr: String): Date? {
    val formats = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    for (format in formats) {
        try {
            val parsedDate = SimpleDateFormat(format, Locale.getDefault()).parse(dateStr)
            if (parsedDate != null) {
                return parsedDate
            }
        } catch (e: Exception) {
            // Thử format tiếp theo
        }
    }

    Log.e(TAG, "Không thể parse chuỗi ngày: $dateStr với bất kỳ định dạng nào")
    return null
}

// Các hàm khác giữ nguyên