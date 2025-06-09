package com.example.movie_booking_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.repository.AuthState
import com.example.movie_booking_app.data.repository.UserRepository
import com.example.movie_booking_app.data.repository.ValidationError
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val userRepository = UserRepository()
    val authState: StateFlow<AuthState> = userRepository.authState

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            userRepository.signIn(email, password)
                .fold(
                    onSuccess = { onResult(true, null) },
                    onFailure = { onResult(false, it.message) }
                )
        }
    }

    fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        birthDay: String = "",
        gender: String = "",
        region: String = "",
        district: String = "",
        favoriteCinema: String = "",
        onResult: (Boolean, String?) -> Unit
    ) {
        val validationError = userRepository.validateSignUpData(email, password, fullName, phone)
        if (validationError != ValidationError.NONE) {
            val errorMessage = when (validationError) {
                ValidationError.INVALID_EMAIL -> "Email không hợp lệ. Vui lòng kiểm tra lại."
                ValidationError.PASSWORD_TOO_SHORT -> "Mật khẩu phải có ít nhất 8 ký tự."
                ValidationError.EMPTY_FULLNAME -> "Họ tên không được để trống."
                ValidationError.INVALID_PHONE -> "Số điện thoại không hợp lệ. Vui lòng nhập đúng định dạng."
                else -> "Dữ liệu không hợp lệ."
            }
            onResult(false, errorMessage)
            return
        }

        viewModelScope.launch {
            userRepository.signUp(email, password, fullName, phone, birthDay, gender, region, district, favoriteCinema)
                .fold(
                    onSuccess = { onResult(true, null) },
                    onFailure = { onResult(false, it.message) }
                )
        }
    }

    fun signOut() {
        userRepository.signOut()
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        if (!userRepository.validateEmail(email)) {
            onResult(false, "Email không hợp lệ.")
            return
        }

        viewModelScope.launch {
            userRepository.resetPassword(email)
                .fold(
                    onSuccess = { onResult(true, "Đã gửi email đặt lại mật khẩu") },
                    onFailure = { onResult(false, it.message ?: "Không thể đặt lại mật khẩu") }
                )
        }
    }
}