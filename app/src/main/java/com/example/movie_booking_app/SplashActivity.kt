package com.example.movie_booking_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Không cần setContentView vì sử dụng windowBackground trong theme

        // Đợi 2.5 giây rồi chuyển đến MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Đóng SplashActivity để người dùng không quay lại được
        }, 2500) // 2.5 giây
    }
}