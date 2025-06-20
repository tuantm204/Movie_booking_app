package com.example.movie_booking_app.ui.screens.Home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
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
import com.example.movie_booking_app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//Top
fun HomeAppBar(
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onTicketsClick: () -> Unit = {}
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
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(33.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        if (isAuthenticated) {
                            onProfileClick()
                        } else {
                            onLoginClick()
                        }
                    }
            ) {
                if (isAuthenticated) {
                    val user = (authState as AuthState.Authenticated).user
                    Text(
                        text = user.displayName?.first()?.toString() ?: "U",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Bold
                    )
                } else {
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
            IconButton(
                onClick = {
                    if (isAuthenticated) {
                        onTicketsClick()
                    } else {
                        onLoginClick()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = "Vé của tôi",
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