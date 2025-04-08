package com.example.movie_booking_app.ui.screens.Home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movie_booking_app.data.repository.AuthState
import com.example.movie_booking_app.data.repository.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    val isAuthenticated = authState is AuthState.Authenticated

    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "MBA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            // Avatar bên trái - điều hướng tùy theo trạng thái đăng nhập
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(33.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        if (isAuthenticated) {
                            // Người dùng đã đăng nhập, mở trang profile
                            onProfileClick()
                        } else {
                            // Người dùng chưa đăng nhập, mở trang đăng nhập
                            onLoginClick()
                        }
                    }
            ) {
                // Hiển thị avatar khác nếu đã đăng nhập
                if (isAuthenticated) {
                    // Có thể hiển thị avatar của người dùng hoặc chữ cái đầu của tên
                    val user = (authState as AuthState.Authenticated).user
                    Text(
                        text = user.displayName?.first()?.toString() ?: "U",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // Icon người dùng mặc định
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Đăng nhập",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                    )
                }
            }
        },
        actions = {
            // Menu 3 gạch bên phải
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        )
    )
}