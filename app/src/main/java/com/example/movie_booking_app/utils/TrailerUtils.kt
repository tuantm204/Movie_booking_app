package com.example.movie_booking_app.utils

//Chuyển đổi URL video thông thường thành URL có chức năng tự động phát (autoplay)
fun getAutoPlayUrl(url: String): String {
    return when {
        url.contains("youtube.com") -> {
            if (url.contains("?")) {
                "$url&autoplay=1&mute=0"
            } else {
                "$url?autoplay=1&mute=0"
            }
        }
        url.contains("youtu.be") -> {
            val videoId = url.substringAfterLast("/")
            "https://www.youtube.com/embed/$videoId?autoplay=1&mute=0"
        }
        else -> url
    }
}