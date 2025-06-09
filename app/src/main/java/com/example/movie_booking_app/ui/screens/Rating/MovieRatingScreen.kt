package com.example.movie_booking_app.ui.screens.Rating

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movie_booking_app.R
import com.example.movie_booking_app.data.model.Booking
import com.example.movie_booking_app.viewmodel.BookingViewModel
import com.example.movie_booking_app.viewmodel.ReviewViewModel
import kotlinx.coroutines.delay

private const val TAG = "MovieRatingScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieRatingScreen(
    bookingId: String,
    reviewViewModel: ReviewViewModel,
    bookingViewModel: BookingViewModel,
    onBackClick: () -> Unit
) {
    val primaryRed = Color(0xFFE71A0F)
    val context = LocalContext.current

    // State cho đánh giá
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    // Flag để kiểm soát việc hiển thị review cũ
    var shouldShowPreviousReview by remember { mutableStateOf(false) }

    // State từ ReviewViewModel
    val currentUserReview by reviewViewModel.userReview.collectAsState()
    val isSubmitting by reviewViewModel.isSubmitting.collectAsState()
    val submitSuccess by reviewViewModel.submitSuccess.collectAsState()
    val error by reviewViewModel.error.collectAsState()

    // State cho booking info
    val allBookings = bookingViewModel.userBookings.collectAsState().value +
            bookingViewModel.pastBookings.collectAsState().value +
            bookingViewModel.upcomingBookings.collectAsState().value

    val booking = allBookings.find { it.id == bookingId }
    val isLoading = bookingViewModel.isLoading.collectAsState().value

    // Khi vào trang, đặt lại các giá trị ban đầu
    LaunchedEffect(Unit) {
        // Reset các giá trị ban đầu
        rating = 0
        comment = ""

        if (allBookings.isEmpty()) {
            bookingViewModel.getUserBookings()
        }
    }

    // Nếu không tìm thấy booking, hiển thị loading hoặc thông báo lỗi
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primaryRed)
        }
        return
    }

    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Không tìm thấy thông tin vé", color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
                ) {
                    Text("Quay lại")
                }
            }
        }
        return
    }

    // Chỉ cập nhật rating từ currentUserReview nhưng không cập nhật comment
    LaunchedEffect(currentUserReview) {
        if (!shouldShowPreviousReview && currentUserReview != null) {
            // Chỉ cập nhật rating, giữ comment rỗng
            rating = currentUserReview?.rating ?: 0
            shouldShowPreviousReview = true
        }
    }

    // Lấy đánh giá hiện tại khi màn hình được mở - nhưng chỉ dùng rating
    LaunchedEffect(booking.movieId) {
        Log.d(TAG, "Đang tải đánh giá trước đó cho phim ${booking.movieTitle} (ID: ${booking.movieId})")
        reviewViewModel.getUserReviewForMovie(booking.movieId)
    }

    // Hiển thị thông báo thành công và quay lại
    if (submitSuccess) {
        LaunchedEffect(submitSuccess) {
            delay(1500) // Đợi 1.5 giây rồi quay lại
            reviewViewModel.clearSubmitStatus()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đánh giá phim") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thông tin phim
                MovieInfoCard(booking, primaryRed)

                Spacer(modifier = Modifier.height(24.dp))

                // Tiêu đề đánh giá
                Text(
                    text = if (currentUserReview != null) "Cập nhật đánh giá của bạn" else "Đánh giá phim này",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rating stars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star $i",
                                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Rating text
                Text(
                    text = when(rating) {
                        0 -> "Chạm vào sao để đánh giá"
                        1 -> "Tệ"
                        2 -> "Không hay"
                        3 -> "Bình thường"
                        4 -> "Hay"
                        else -> "Rất hay"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (rating > 0) primaryRed else Color.Gray,
                    fontWeight = if (rating > 0) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Comment field - luôn trống khi vào trang
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Nhận xét của bạn") },
                    placeholder = { Text("Chia sẻ cảm nhận về phim...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        cursorColor = primaryRed
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error message nếu có
                if (error != null) {
                    Text(
                        text = error ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Submit button
                Button(
                    onClick = {
                        if (rating > 0) {
                            Log.d(TAG, "Gửi đánh giá: $rating sao, comment: $comment")
                            reviewViewModel.submitReview(booking.movieId, rating, comment)
                        }
                    },
                    enabled = rating > 0 && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryRed,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Gửi đánh giá",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel button
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryRed
                    )
                ) {
                    Text(
                        text = "Huỷ bỏ",
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MovieInfoCard(booking: Booking, primaryRed: Color) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster phim
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(booking.movieImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Movie poster",
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                fallback = painterResource(id = R.drawable.placeholder_poster),
                error = painterResource(id = R.drawable.placeholder_poster)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Thông tin phim
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = booking.movieTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Thông tin thời gian xem
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = primaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.showDate,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = primaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.showTime,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hiển thị tên rạp nếu có
                if (booking.theaterName.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = primaryRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = booking.theaterName,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}