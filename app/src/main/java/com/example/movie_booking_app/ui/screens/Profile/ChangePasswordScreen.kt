package com.example.movie_booking_app.ui.screens.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movie_booking_app.data.repository.AuthViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val primaryRed = Color(0xFFE71A0F)  // Màu đỏ chính
    val darkGray = Color(0xFF333333)    // Màu xám tối cho text

    // State cho form
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // State ẩn hiện mật khẩu
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // State cho lỗi
    var currentPasswordError by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var generalError by remember { mutableStateOf("") }

    // State cho loading và success
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Coroutine scope
    val scope = rememberCoroutineScope()

    // Hàm validate input
    fun validateInput(): Boolean {
        var isValid = true

        // Reset errors
        currentPasswordError = ""
        newPasswordError = ""
        confirmPasswordError = ""
        generalError = ""

        // Validate current password
        if (currentPassword.isEmpty()) {
            currentPasswordError = "Vui lòng nhập mật khẩu hiện tại"
            isValid = false
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            newPasswordError = "Vui lòng nhập mật khẩu mới"
            isValid = false
        } else if (newPassword.length < 6) {
            newPasswordError = "Mật khẩu phải có ít nhất 6 ký tự"
            isValid = false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordError = "Vui lòng xác nhận mật khẩu mới"
            isValid = false
        } else if (confirmPassword != newPassword) {
            confirmPasswordError = "Mật khẩu xác nhận không khớp"
            isValid = false
        }

        return isValid
    }

    // Hàm thay đổi mật khẩu
    fun changePassword() {
        if (!validateInput()) return

        isLoading = true
        generalError = ""

        scope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser

                if (user != null && user.email != null) {
                    // Xác thực lại người dùng với mật khẩu hiện tại
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                    // Xác thực lại trước khi đổi mật khẩu
                    user.reauthenticate(credential).await()

                    // Đổi mật khẩu
                    user.updatePassword(newPassword).await()

                    // Đổi mật khẩu thành công
                    isSuccess = true

                    // Reset form
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                }
            } catch (e: Exception) {
                e.message?.let {
                    generalError = when {
                        it.contains("password is invalid") -> "Mật khẩu hiện tại không chính xác"
                        it.contains("network") -> "Lỗi kết nối mạng"
                        else -> "Đổi mật khẩu không thành công: ${e.message}"
                    }
                } ?: run {
                    generalError = "Đã xảy ra lỗi không xác định"
                }
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đổi mật khẩu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon mật khẩu
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(16.dp),
                    tint = primaryRed
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tiêu đề và giải thích
                Text(
                    text = "Đổi mật khẩu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkGray
                )

                Text(
                    text = "Nhập mật khẩu hiện tại và mật khẩu mới của bạn",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Form đổi mật khẩu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Mật khẩu hiện tại
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Mật khẩu hiện tại") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                    Icon(
                                        imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (currentPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                                    )
                                }
                            },
                            isError = currentPasswordError.isNotEmpty(),
                            supportingText = {
                                if (currentPasswordError.isNotEmpty()) {
                                    Text(currentPasswordError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryRed,
                                focusedLabelColor = primaryRed,
                                cursorColor = primaryRed
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mật khẩu mới
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Mật khẩu mới") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (newPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                                    )
                                }
                            },
                            isError = newPasswordError.isNotEmpty(),
                            supportingText = {
                                if (newPasswordError.isNotEmpty()) {
                                    Text(newPasswordError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryRed,
                                focusedLabelColor = primaryRed,
                                cursorColor = primaryRed
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Xác nhận mật khẩu mới
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Xác nhận mật khẩu mới") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                                    )
                                }
                            },
                            isError = confirmPasswordError.isNotEmpty(),
                            supportingText = {
                                if (confirmPasswordError.isNotEmpty()) {
                                    Text(confirmPasswordError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryRed,
                                focusedLabelColor = primaryRed,
                                cursorColor = primaryRed
                            )
                        )

                        // Hiển thị lỗi chung nếu có
                        if (generalError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = generalError,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Nút đổi mật khẩu
                        Button(
                            onClick = { changePassword() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryRed,
                                contentColor = Color.White
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Xác nhận")
                            }
                        }
                    }
                }
            }

            // Hiển thị dialog khi đổi mật khẩu thành công
            if (isSuccess) {
                AlertDialog(
                    onDismissRequest = {
                        isSuccess = false
                        onBackClick()
                    },
                    title = { Text("Thành công") },
                    text = { Text("Đổi mật khẩu thành công!") },
                    confirmButton = {
                        Button(
                            onClick = {
                                isSuccess = false
                                onBackClick()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryRed
                            )
                        ) {
                            Text("Đóng")
                        }
                    },
                    containerColor = Color.White
                )
            }
        }
    }
}