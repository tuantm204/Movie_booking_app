package com.example.movie_booking_app.data.repository

import com.example.movie_booking_app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
                fetchUserData(user.uid)
            } else {
                _authState.value = AuthState.Unauthenticated
                _currentUser.value = null
            }
        }
    }

    fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
            fetchUserData(currentUser.uid)
        } else {
            _authState.value = AuthState.Unauthenticated
            _currentUser.value = null
        }
    }

    // Phiên bản không-suspend cho các hàm callback
    fun fetchUserData(userId: String) {
        scope.launch {
            loadUserData(userId)
        }
    }
    //Phiên bản suspend dành cho các hàm suspend gọi
    suspend fun loadUserData(userId: String) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (userDoc.exists() && userDoc.data != null) {
                _currentUser.value = User.fromMap(userId, userDoc.data!!)
            }
        } catch (e: Exception) {
        }
    }

    //Đăng nhập
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            _authState.value = AuthState.Loading
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                _authState.value = AuthState.Authenticated(it)
                loadUserData(it.uid)
                Result.success(it)
            } ?: Result.failure(Exception("Đăng nhập thất bại"))
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Đăng nhập thất bại")
            Result.failure(e)
        }
    }

    // Hàm validation cho từng trường
    fun validateEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 8
    }

    fun validateFullName(fullName: String): Boolean {
        return fullName.trim().isNotEmpty()
    }

    fun validatePhone(phone: String): Boolean {
        return PHONE_PATTERN.matcher(phone).matches()
    }

    // Hàm kiểm tra tất cả các ràng buộc trước khi đăng ký
    fun validateSignUpData(email: String, password: String, fullName: String, phone: String): ValidationError {
        return when {
            !validateEmail(email) -> ValidationError.INVALID_EMAIL
            !validatePassword(password) -> ValidationError.PASSWORD_TOO_SHORT
            !validateFullName(fullName) -> ValidationError.EMPTY_FULLNAME
            !validatePhone(phone) -> ValidationError.INVALID_PHONE
            else -> ValidationError.NONE
        }
    }

    //Đăng ký
    suspend fun signUp(
        email: String, 
        password: String, 
        fullName: String, 
        phone: String, 
        birthDay: String = "", 
        gender: String = "", 
        region: String = "", 
        district: String = "", 
        favoriteCinema: String = ""
    ): Result<FirebaseUser> {
        return try {
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
                Result.success(user)
            } ?: Result.failure(Exception("Đăng ký thất bại"))
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            if (e is FirebaseAuthUserCollisionException) {
                Result.failure(Exception("Email này đã được sử dụng. Vui lòng thử email khác hoặc quên mật khẩu."))
            } else {
                Result.failure(e)
            }
        }
    }

    //Cập nhật
    suspend fun updateUserProfile(
        fullName: String? = null,
        phone: String? = null,
        birthDay: String? = null,
        gender: String? = null,
        region: String? = null,
        district: String? = null,
        favoriteCinema: String? = null,
    ): Result<User> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Người dùng chưa đăng nhập"))
        val currentUserData = _currentUser.value ?: return Result.failure(Exception("Không tìm thấy thông tin người dùng"))

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

        return try {
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
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //Đăng xuất
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _currentUser.value = null
    }

    //ĐỔi mật khẩu
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}