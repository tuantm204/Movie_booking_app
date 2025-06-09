package com.example.movie_booking_app.data.network

import android.util.Log
import com.example.movie_booking_app.data.model.Room
import com.example.movie_booking_app.data.model.SeatRow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "RoomsFirestore"

/**
 * Lấy tất cả các phòng chiếu từ Firestore
 */
suspend fun getAllRoomsFromFirestore(): List<Room> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("Rooms")
            .get()
            .await()

        snapshot.documents.map { doc ->
            Room(
                roomId = doc.id,
                theaterId = doc.getString("theaterId") ?: "",
                name = doc.getString("name") ?: "",
                seatingCapacity = doc.getLong("seatingCapacity")?.toInt() ?: 0,
                screenType = doc.getString("screenType") ?: "",
                availableSeats = doc.getLong("availableSeats")?.toInt() ?: 0,
                facilities = doc.get("facilities") as? List<String> ?: emptyList(),
                seatMatrix = parseSeatMatrix(doc.get("seatMatrix"))
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "Lỗi khi lấy tất cả phòng: ${e.message}", e)
        emptyList()
    }
}

/**
 * Lấy các phòng chiếu theo ID rạp
 */
suspend fun getRoomsByTheaterId(theaterId: String): List<Room> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("Rooms")
            .whereEqualTo("theaterId", theaterId)
            .get()
            .await()

        snapshot.documents.map { doc ->
            Room(
                roomId = doc.id,
                theaterId = doc.getString("theaterId") ?: "",
                name = doc.getString("name") ?: "",
                seatingCapacity = doc.getLong("seatingCapacity")?.toInt() ?: 0,
                screenType = doc.getString("screenType") ?: "",
                availableSeats = doc.getLong("availableSeats")?.toInt() ?: 0,
                facilities = doc.get("facilities") as? List<String> ?: emptyList(),
                seatMatrix = parseSeatMatrix(doc.get("seatMatrix"))
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "Lỗi khi lấy phòng theo rạp $theaterId: ${e.message}", e)
        emptyList()
    }
}

/**
 * Lấy thông tin một phòng chiếu theo ID
 */
suspend fun getRoomById(roomId: String): Room? {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        Log.d(TAG, "Đang tìm thông tin phòng $roomId")
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
                seatMatrix = parseSeatMatrix(doc.get("seatMatrix"))
            )
        } else {
            Log.d(TAG, "Không tìm thấy phòng $roomId")
            null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Lỗi khi lấy thông tin phòng $roomId: ${e.message}", e)
        null
    }
}

/**
 * Chuyển đổi dữ liệu seatMatrix từ Firestore sang List<SeatRow>
 */
private fun parseSeatMatrix(seatMatrixData: Any?): List<SeatRow> {
    val seatRows = mutableListOf<SeatRow>()

    when (seatMatrixData) {
        is List<*> -> {
            // Cấu trúc là mảng các map
            for (item in seatMatrixData) {
                if (item is Map<*, *>) {
                    val rowName = item["row"] as? String ?: ""
                    val types = item["types"] as? List<String> ?: emptyList()
                    seatRows.add(SeatRow(row = rowName, types = types))
                }
            }
        }
        is Map<*, *> -> {
            // Cấu trúc cũ là map
            val rows = seatMatrixData.keys.filterIsInstance<String>()
            for (row in rows) {
                val types = when (val typesValue = seatMatrixData[row]) {
                    is String -> listOf(typesValue)
                    is List<*> -> typesValue.filterIsInstance<String>()
                    else -> emptyList()
                }
                seatRows.add(SeatRow(row = row, types = types))
            }
        }
    }

    return seatRows
}