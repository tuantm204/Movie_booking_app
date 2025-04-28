package com.example.movie_booking_app.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_booking_app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern

// Các trạng thái
sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

// Enum class cho các loại lỗi validation
enum class ValidationError {
    INVALID_EMAIL,
    PASSWORD_TOO_SHORT,
    EMPTY_FULLNAME,
    INVALID_PHONE,
    EMAIL_ALREADY_EXISTS,
    NONE
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%-+]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" +
                "(" +
                "." +
                "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" +
                ")+"
    )

    private val PHONE_PATTERN = Pattern.compile("^(0|\\+84)(3|5|7|8|9)\\d{8}$")

    init {
        checkCurrentUser()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _authState.value = AuthState.Authenticated(user)
                loadUserData(user.uid)
            } else {
                _authState.value = AuthState.Unauthenticated
                _currentUser.value = null
            }
        }
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
            loadUserData(currentUser.uid)
        } else {
            _authState.value = AuthState.Unauthenticated
            _currentUser.value = null
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                if (userDoc.exists() && userDoc.data != null) {
                    _currentUser.value = User.fromMap(userId, userDoc.data!!)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let {
                    _authState.value = AuthState.Authenticated(it)
                    loadUserData(it.uid)
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

    // Hàm validation cho từng trường
    private fun validateEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun validateFullName(fullName: String): Boolean {
        return fullName.trim().isNotEmpty()
    }

    private fun validatePhone(phone: String): Boolean {
        return PHONE_PATTERN.matcher(phone).matches()
    }

    // Hàm kiểm tra tất cả các ràng buộc trước khi đăng ký
    fun validateSignUpData(
        email: String,
        password: String,
        fullName: String,
        phone: String
    ): ValidationError {
        return when {
            !validateEmail(email) -> ValidationError.INVALID_EMAIL
            !validatePassword(password) -> ValidationError.PASSWORD_TOO_SHORT
            !validateFullName(fullName) -> ValidationError.EMPTY_FULLNAME
            !validatePhone(phone) -> ValidationError.INVALID_PHONE
            else -> ValidationError.NONE
        }
    }

    fun signUp(email: String, password: String, fullName: String, phone: String, birthDay: String = "", gender: String = "", region: String = "", district: String = "", favoriteCinema: String = "", onResult: (Boolean, String?) -> Unit
    ) {
        val validationError = validateSignUpData(email, password, fullName, phone)
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
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                result.user?.let { user ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                    val newUser = User(
                        id = user.uid,
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        birthDay = birthDay,
                        gender = gender,
                        region = region,
                        district = district,
                        favoriteCinema = favoriteCinema,
                        createdAt = System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(newUser.toMap())
                        .await()

                    _currentUser.value = newUser
                    _authState.value = AuthState.Authenticated(user)
                    onResult(true, null)
                } ?: run {
                    _authState.value = AuthState.Unauthenticated
                    onResult(false, "Đăng ký thất bại")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated

                if (e is FirebaseAuthUserCollisionException) {
                    onResult(false, "Email này đã được sử dụng. Vui lòng thử email khác hoặc đăng nhập.")
                } else {
                    onResult(false, e.message ?: "Đăng ký thất bại")
                }
            }
        }
    }

    fun updateUserProfile(
        fullName: String? = null,
        phone: String? = null,
        birthDay: String? = null,
        gender: String? = null,
        region: String? = null,
        district: String? = null,
        favoriteCinema: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        // Kiểm tra validation cho các trường được cập nhật
        if (fullName != null && !validateFullName(fullName)) {
            onResult(false, "Họ tên không được để trống.")
            return
        }

        if (phone != null && !validatePhone(phone)) {
            onResult(false, "Số điện thoại không hợp lệ.")
            return
        }

        val currentUserId = auth.currentUser?.uid ?: run {
            onResult(false, "Người dùng chưa đăng nhập")
            return
        }

        val currentUserData = _currentUser.value ?: run {
            onResult(false, "Không tìm thấy thông tin người dùng")
            return
        }

        // Tạo User mới với các thông tin được cập nhật
        val updatedUser = currentUserData.copy(
            fullName = fullName ?: currentUserData.fullName,
            phone = phone ?: currentUserData.phone,
            birthDay = birthDay ?: currentUserData.birthDay,
            gender = gender ?: currentUserData.gender,
            region = region ?: currentUserData.region,
            district = district ?: currentUserData.district,
            favoriteCinema = favoriteCinema ?: currentUserData.favoriteCinema
        )

        viewModelScope.launch {
            try {
                // Cập nhật displayName nếu có thay đổi
                if (fullName != null && fullName != currentUserData.fullName) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    auth.currentUser?.updateProfile(profileUpdates)?.await()
                }

                // Cập nhật dữ liệu trong Firestore
                firestore.collection("users")
                    .document(currentUserId)
                    .update(updatedUser.toMap())
                    .await()

                _currentUser.value = updatedUser
                onResult(true, "Cập nhật thông tin thành công")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Cập nhật thông tin thất bại")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _currentUser.value = null
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        if (!validateEmail(email)) {
            onResult(false, "Email không hợp lệ.")
            return
        }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onResult(true, "Đã gửi email đặt lại mật khẩu")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Không thể đặt lại mật khẩu")
            }
        }
    }
}