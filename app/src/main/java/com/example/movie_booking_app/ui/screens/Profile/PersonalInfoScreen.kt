package com.example.movie_booking_app.ui.screens.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movie_booking_app.data.repository.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val primaryRed = Color(0xFFE71A0F)  // Màu đỏ chính
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

    // State chỉnh sửa
    var isEditing by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var favoriteCinema by remember { mutableStateOf("") }

    // State thông báo
    var showSuccessMessage by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    // Lấy thông tin chi tiết từ Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { userId ->
            try {
                val document = firestore.collection("users").document(userId).get().await()
                if (document.exists()) {
                    userDetails = document.data

                    // Khởi tạo giá trị cho các trường chỉnh sửa
                    phone = userDetails?.get("phone") as? String ?: ""
                    birthDay = userDetails?.get("birthDay") as? String ?: ""
                    gender = userDetails?.get("gender") as? String ?: ""
                    region = userDetails?.get("region") as? String ?: ""
                    district = userDetails?.get("district") as? String ?: ""
                    favoriteCinema = userDetails?.get("favoriteCinema") as? String ?: ""
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            } finally {
                isLoading = false
            }
        }
    }

    // Hàm lưu thông tin đã chỉnh sửa
    fun saveUserDetails() {
        user?.uid?.let { userId ->
            isSaving = true
            val updatedData = hashMapOf<String, Any>(
                "phone" to phone,
                "birthDay" to birthDay,
                "gender" to gender,
                "region" to region,
                "district" to district,
                "favoriteCinema" to favoriteCinema
            )

            scope.launch {
                try {
                    firestore.collection("users")
                        .document(userId)
                        .update(updatedData)
                        .await()

                    // Cập nhật lại userDetails
                    userDetails = userDetails?.toMutableMap()?.apply {
                        putAll(updatedData)
                    }

                    isEditing = false
                    showSuccessMessage = true
                } catch (e: Exception) {
                    // Xử lý lỗi khi cập nhật
                } finally {
                    isSaving = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thông tin cá nhân",
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
                ),
                actions = {
                    // Nút chỉnh sửa/lưu
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                saveUserDetails()
                            } else {
                                isEditing = true
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Lưu" else "Chỉnh sửa",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (isLoading || isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryRed
                )
            } else if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
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

                    // Email
                    Text(
                        text = user.email ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Thông tin chi tiết người dùng
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Thông tin cá nhân",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = darkGray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Số điện thoại
                            if (isEditing) {
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Số điện thoại") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = primaryRed
                                        )
                                    }
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = primaryRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Số điện thoại",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            phone.ifEmpty { "Chưa cập nhật" },
                                            fontSize = 16.sp,
                                            color = darkGray
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Ngày sinh
                            if (isEditing) {
                                OutlinedTextField(
                                    value = birthDay,
                                    onValueChange = { birthDay = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Ngày sinh (DD/MM/YYYY)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = primaryRed
                                        )
                                    }
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = primaryRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Ngày sinh",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            birthDay.ifEmpty { "Chưa cập nhật" },
                                            fontSize = 16.sp,
                                            color = darkGray
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Giới tính
                            if (isEditing) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "Giới tính",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = gender == "Nam",
                                            onClick = { gender = "Nam" },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = primaryRed
                                            )
                                        )
                                        Text("Nam", fontSize = 16.sp)

                                        Spacer(modifier = Modifier.width(24.dp))

                                        RadioButton(
                                            selected = gender == "Nữ",
                                            onClick = { gender = "Nữ" },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = primaryRed
                                            )
                                        )
                                        Text("Nữ", fontSize = 16.sp)
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = primaryRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Giới tính",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            gender.ifEmpty { "Chưa cập nhật" },
                                            fontSize = 16.sp,
                                            color = darkGray
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Khu vực
                            if (isEditing) {
                                OutlinedTextField(
                                    value = region,
                                    onValueChange = { region = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Tỉnh/Thành phố") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = primaryRed
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Quận/Huyện") }
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = primaryRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Khu vực",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            if (region.isNotEmpty()) "$region ${if (district.isNotEmpty()) "- $district" else ""}" else "Chưa cập nhật",
                                            fontSize = 16.sp,
                                            color = darkGray
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            // Rạp yêu thích
                            if (isEditing) {
                                OutlinedTextField(
                                    value = favoriteCinema,
                                    onValueChange = { favoriteCinema = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Rạp yêu thích") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = primaryRed
                                        )
                                    }
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = primaryRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Rạp yêu thích",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            favoriteCinema.ifEmpty { "Chưa cập nhật" },
                                            fontSize = 16.sp,
                                            color = darkGray
                                        )
                                    }
                                }
                            }

                            if (isEditing) {
                                Spacer(modifier = Modifier.height(24.dp))

                                // Nút lưu khi đang chỉnh sửa
                                Button(
                                    onClick = { saveUserDetails() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryRed
                                    ),
                                    enabled = !isSaving
                                ) {
                                    Text("Lưu thông tin")
                                }

                                TextButton(
                                    onClick = { isEditing = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Hủy")
                                }
                            }
                        }
                    }
                }

                // Hiển thị thông báo thành công
                if (showSuccessMessage) {
                    AlertDialog(
                        onDismissRequest = {
                            showSuccessMessage = false
                            onBackClick()
                        },
                        title = { Text("Thành công") },
                        text = { Text("Cập nhật thông tin thành công!") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSuccessMessage = false
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
}