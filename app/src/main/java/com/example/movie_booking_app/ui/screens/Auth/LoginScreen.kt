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
    val primaryRed = Color(0xFFE71A0F)  // Màu đỏ chính của CGV
    val darkGray = Color(0xFF333333)    // Màu xám tối cho text

    // Theo dõi trạng thái đăng nhập từ AuthViewModel
    val authState by authViewModel.authState.collectAsState()

    // Xử lý điều hướng và trạng thái sau khi trạng thái auth thay đổi
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // Đăng nhập thành công
                isLoading = false
                successMessage = "Đăng nhập thành công!"
                delay(500) // Đợi 0.5s để hiển thị thông báo thành công
                onLoginSuccess() // Điều hướng đến màn hình chính
            }
            is AuthState.Error -> {
                // Đăng nhập thất bại
                isLoading = false
                errorMessage = (authState as AuthState.Error).message
            }
            AuthState.Loading -> {
                // Đang xử lý đăng nhập
                isLoading = true
            }
            AuthState.Unauthenticated -> {
                // Chưa đăng nhập
                isLoading = false
            }
        }
    }

    // Bố cục màn hình
    Scaffold(
        // Thanh tiêu đề với nút quay lại
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đăng Nhập", // Tiêu đề màn hình
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
                    containerColor = primaryRed, // Nền màu đỏ cho TopAppBar
                    titleContentColor = Color.White, // Chữ màu trắng
                    navigationIconContentColor = Color.White // Icon màu trắng
                )
            )
        }
    ) { paddingValues ->
        // Nội dung chính của màn hình
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Cho phép cuộn khi bàn phím hiện
                .background(Color.White), // Nền trắng
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Banner hình ảnh
            Image(
                painter = painterResource(id = R.drawable.banner01),
                contentDescription = "Banner CGV",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Chiều cao cố định cho banner
                contentScale = ContentScale.Crop // Cắt hình để vừa với khung
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Form đăng nhập
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp) // Padding hai bên
                    .fillMaxWidth()
            ) {
                // Trường nhập email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        // Xóa thông báo lỗi khi người dùng nhập lại
                        errorMessage = null
                    },
                    label = { Text("Email hoặc số điện thoại") },
                    leadingIcon = {
                        // Icon email ở đầu trường
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    singleLine = true, // Không cho phép xuống dòng
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next // Nút Next trên bàn phím
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed, // Viền khi focus
                        focusedLabelColor = primaryRed, // Màu label khi focus
                        cursorColor = primaryRed // Màu con trỏ
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Trường nhập mật khẩu
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        // Xóa thông báo lỗi khi người dùng nhập lại
                        errorMessage = null
                    },
                    label = { Text("Mật khẩu") },
                    leadingIcon = {
                        // Icon khóa ở đầu trường
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        // Nút hiện/ẩn mật khẩu
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
                    // Ẩn mật khẩu khi passwordVisible = false
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done // Nút Done trên bàn phím
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed,
                        cursorColor = primaryRed
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nút quên mật khẩu
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
                        modifier = Modifier.align(Alignment.CenterEnd) // Đặt nút bên phải
                    ) {
                        Text(
                            "Quên mật khẩu?",
                            color = Color.Blue
                        )
                    }
                }

                // Hiển thị thông báo lỗi nếu có
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Hiển thị thông báo thành công nếu có
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

                // Nút đăng nhập
                Button(
                    onClick = {
                        // Xóa thông báo lỗi/thành công hiện tại
                        errorMessage = null
                        successMessage = null

                        // Kiểm tra email/password trước khi gửi
                        if (email.isEmpty() || password.isEmpty()) {
                            errorMessage = "Vui lòng nhập đầy đủ thông tin"
                            return@Button
                        }

                        // Gọi hàm đăng nhập từ ViewModel
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
                        containerColor = primaryRed // Màu đỏ cho nút
                    ),
                    shape = RoundedCornerShape(4.dp),
                    enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
                ) {
                    // Hiển thị progress indicator khi đang loading
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

                // Phân cách "hoặc"
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

                // Nút đăng ký
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