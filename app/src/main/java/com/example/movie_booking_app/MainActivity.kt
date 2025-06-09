package com.example.movie_booking_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.movie_booking_app.ui.navigation.AppNavigation
import com.example.movie_booking_app.ui.theme.MoviebookingappTheme
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo ZaloPay SDK đúng chuẩn demo
        ZaloPaySDK.init(553, Environment.SANDBOX)

        // Kiểm tra xem có phải được khởi động để điều hướng không
        val navigateTo = intent.getStringExtra("navigate_to")
        val showtimeId = intent.getStringExtra("showtime_id")

        setContent {
            MoviebookingappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation()
                    if (navigateTo == "ticket-confirmation" && !showtimeId.isNullOrEmpty()) {
                        navController.navigate("ticket-confirmation/$showtimeId") {
                            popUpTo("home") {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Thử gọi ZaloPaySDK.getInstance().onResult(intent) và bắt ngoại lệ nếu có
        try {
            val result = ZaloPaySDK.getInstance().onResult(intent)
        } catch (e: Exception) {
        }
        if (intent.data != null || intent.dataString != null) {
            val uriString = intent.dataString ?: intent.data.toString()
            // Kiểm tra cả trường hợp scheme khác
            if (uriString.startsWith("moviebookingapp://") ||
                uriString.startsWith("moviebookingapp://app") ||
                uriString.contains("moviebookingapp")) {
                handleZaloPayCallback()
            }
        }
    }

    // Tách logic xử lý callback thành hàm riêng để dễ quản lý
    private fun handleZaloPayCallback() {
        try {
            val prefs = getSharedPreferences("zalo_pay", MODE_PRIVATE)
            val shouldRedirect = prefs.getBoolean("should_redirect_to_confirmation", false)
            val bookingId = prefs.getString("last_booking_id", "") ?: ""
            if (shouldRedirect && bookingId.isNotEmpty()) {
                prefs.edit()
                    .putBoolean("should_redirect_to_confirmation", false)
                    .putBoolean("payment_pending", false)
                    .apply()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("navigate_to", "ticket-confirmation")
                intent.putExtra("booking_id", bookingId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Lỗi khi xử lý callback ZaloPay", e)
        }
    }
}