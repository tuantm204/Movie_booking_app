package com.example.movie_booking_app.ui.screens.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movie_booking_app.data.repository.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onPersonalInfoClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onTransactionHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val primaryRed = Color(0xFFE71A0F)  // Màu đỏ chính của CGV
    val darkGray = Color(0xFF333333)    // Màu xám tối cho text

    // Lấy thông tin user hiện tại từ AuthViewModel
    val authState by authViewModel.authState.collectAsState()
    val user = remember(authState) {
        if (authState is com.example.movie_booking_app.data.repository.AuthState.Authenticated) {
            (authState as com.example.movie_booking_app.data.repository.AuthState.Authenticated).user
        } else null
    }

    // State để lưu thông tin chi tiết của user từ Firestore
    var userDetails by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Lấy thông tin chi tiết từ Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { userId ->
            try {
                val firestore = FirebaseFirestore.getInstance()
                val document = firestore.collection("users").document(userId).get().await()
                if (document.exists()) {
                    userDetails = document.data
                }
            } catch (e: Exception) {
                // Xử lý lỗi
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
                        "Thành viên MBA",
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryRed
                )
            } else if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(primaryRed.copy(alpha = 0.2f))
                            .border(2.dp, primaryRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName?.firstOrNull()?.toString() ?: "U",
                            color = primaryRed,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tên người dùng
                    Text(
                        text = user.displayName ?: "Người dùng",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkGray
                    )

                    // Thẻ thành viên
                    Card(
                        modifier = Modifier.padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = primaryRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "MEMBER",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = primaryRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Menu tùy chọn
                    MenuOption(
                        icon = Icons.Default.Person,
                        title = "Thông tin cá nhân",
                        onClick = onPersonalInfoClick,
                        primaryRed = primaryRed,
                        darkGray = darkGray
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    MenuOption(
                        icon = Icons.Default.Lock,
                        title = "Đổi mật khẩu",
                        onClick = onChangePasswordClick,
                        primaryRed = primaryRed,
                        darkGray = darkGray
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    MenuOption(
                        icon = Icons.Default.History,
                        title = "Lịch sử giao dịch",
                        onClick = onTransactionHistoryClick,
                        primaryRed = primaryRed,
                        darkGray = darkGray
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            authViewModel.signOut()
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đăng xuất")
                    }
                }
            } else {
                // Nếu không có user (không đáng xảy ra vì đã kiểm tra auth)
                Text(
                    "Vui lòng đăng nhập để xem thông tin",
                    modifier = Modifier.align(Alignment.Center),
                    color = darkGray
                )
            }
        }
    }
}

@Composable
fun MenuOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    primaryRed: Color,
    darkGray: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryRed,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = darkGray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = darkGray.copy(alpha = 0.7f)
        )
    }
}