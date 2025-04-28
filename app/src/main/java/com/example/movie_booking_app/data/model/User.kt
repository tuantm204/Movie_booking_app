package com.example.movie_booking_app.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val birthDay: String = "",
    val gender: String = "",
    val region: String = "",
    val district: String = "",
    val favoriteCinema: String = "",
    val createdAt: Long = 0L
) {
    // Chuyển đổi đối tượng User thành Map để lưu vào Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "birthDay" to birthDay,
            "gender" to gender,
            "region" to region,
            "district" to district,
            "favoriteCinema" to favoriteCinema,
            "createdAt" to createdAt
        )
    }

    companion object {
        // Tạo đối tượng User từ document Firestore
        fun fromMap(id: String, data: Map<String, Any?>): User {
            return User(
                id = id,
                fullName = data["fullName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                birthDay = data["birthDay"] as? String ?: "",
                gender = data["gender"] as? String ?: "",
                region = data["region"] as? String ?: "",
                district = data["district"] as? String ?: "",
                favoriteCinema = data["favoriteCinema"] as? String ?: "",
                createdAt = data["createdAt"] as? Long ?: 0L
            )
        }
    }
}