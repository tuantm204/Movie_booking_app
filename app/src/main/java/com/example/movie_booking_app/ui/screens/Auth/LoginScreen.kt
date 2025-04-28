package com.example.movie_booking_app.ui.screens.Auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movie_booking_app.R
import com.example.movie_booking_app.data.repository.AuthState
import com.example.movie_booking_app.data.repository.AuthViewModel
import kotlinx.coroutines.delay

/**
 * Màn hình đăng nhập với Firebase Authentication
 *
 * @param authViewModel ViewModel xử lý logic đăng nhập với Firebase
 * @param onBackClick Callback khi người dùng bấm nút quay lại
 * @param onRegisterClick Callback khi người dùng bấm nút đăng ký
 * @param onLoginSuccess Callback khi đăng nhập thành công
 * @param onForgotPasswordClick Callback khi người dùng bấm quên mật khẩu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit = {}
) {
    // State quản lý dữ liệu form
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // Kiểm soát hiển thị mật khẩu

    // State quản lý trạng thái UI
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Màu sắc chính
    val primaryRed = Color(0xFFE71A0F)
    val darkGray = Color(0xFF333333)

    // Theo dõi trạng thái đăng nhập từ AuthViewModel
    val authState by authViewModel.authState.collectAsState()

    // Xử lý điều hướng và trạng thái sau khi trạng thái auth thay đổi
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                isLoading = false
                successMessage = "Đăng nhập thành công!"
                onLoginSuccess()
            }
            is AuthState.Error -> {
                isLoading = false
                errorMessage = (authState as AuthState.Error).message
            }
            AuthState.Loading -> {
                isLoading = true
            }
            AuthState.Unauthenticated -> {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đăng Nhập",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner01),
                contentDescription = "Banner CGV",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Form đăng nhập
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                // Trường nhập email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email hoặc số điện thoại") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed,
                        cursorColor = primaryRed
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Trường nhập mật khẩu
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Mật khẩu") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible)
                                    "Ẩn mật khẩu" else "Hiện mật khẩu"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed,
                        cursorColor = primaryRed
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            if (email.isNotEmpty()) {
                                // Gọi hàm resetPassword từ AuthViewModel
                                authViewModel.resetPassword(email) { success, message ->
                                    if (success) {
                                        successMessage = "Đã gửi email đặt lại mật khẩu!"
                                    } else {
                                        errorMessage = message ?: "Không thể đặt lại mật khẩu"
                                    }
                                }
                            } else {
                                errorMessage = "Vui lòng nhập email để đặt lại mật khẩu"
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            "Quên mật khẩu?",
                            color = Color.Blue
                        )
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                successMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Green,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null

                        if (email.isEmpty() || password.isEmpty()) {
                            errorMessage = "Vui lòng nhập đầy đủ thông tin"
                            return@Button
                        }

                        authViewModel.signIn(email, password) { success, message ->
                            if (!success) {
                                errorMessage = message ?: "Đăng nhập thất bại"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryRed
                    ),
                    shape = RoundedCornerShape(4.dp),
                    enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "ĐĂNG NHẬP",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                    Text(
                        text = " hoặc ",
                        color = darkGray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Đăng ký tài khoản CGV",
                        color = darkGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}