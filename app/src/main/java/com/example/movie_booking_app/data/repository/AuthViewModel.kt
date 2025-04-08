package com.example.movie_booking_app.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        // Kiểm tra trạng thái đăng nhập hiện tại khi ViewModel được khởi tạo
        checkCurrentUser()

        // Đăng ký lắng nghe thay đổi trạng thái đăng nhập
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _authState.value = AuthState.Authenticated(user)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let {
                    _authState.value = AuthState.Authenticated(it)
                    onResult(true, null)
                } ?: run {
                    _authState.value = AuthState.Unauthenticated
                    onResult(false, "Đăng nhập thất bại")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Đăng nhập thất bại")
                onResult(false, e.message)
            }
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
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                result.user?.let { user ->
                    // Cập nhật displayName
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    user.updateProfile(profileUpdates).await()

                    // Lưu thông tin chi tiết của user vào Firestore
                    val userMap = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "phone" to phone,
                        "birthDay" to birthDay,
                        "gender" to gender,
                        "region" to region,
                        "district" to district,
                        "favoriteCinema" to favoriteCinema,
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(userMap)
                        .await()

                    _authState.value = AuthState.Authenticated(user)
                    onResult(true, null)
                } ?: run {
                    _authState.value = AuthState.Unauthenticated
                    onResult(false, "Đăng ký thất bại")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Đăng ký thất bại")
                onResult(false, e.message)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onResult(true, "Đã gửi email đặt lại mật khẩu")
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}