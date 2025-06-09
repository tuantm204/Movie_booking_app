package com.example.movie_booking_app.ui.screens.Profile

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.util.Log
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movie_booking_app.R
import com.example.movie_booking_app.data.model.Booking
import com.example.movie_booking_app.data.model.Review
import com.example.movie_booking_app.viewmodel.ReviewViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    booking: Booking,
    reviewViewModel: ReviewViewModel,
    isPastBooking: Boolean = false,
    onBackClick: () -> Unit,
    onRateMovieClick: (Booking) -> Unit = {}
) {
    Log.d(TAG, "TicketDetailScreen displayed with booking: ${booking.id} - ${booking.movieTitle}")
    val primaryRed = Color(0xFFE71A0F)
    val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    // State từ ReviewViewModel
    val ratedMovieIds by reviewViewModel.ratedMovieIds.collectAsState()
    val isRated = ratedMovieIds.contains(booking.movieId)
    val currentUserReview by reviewViewModel.userReview.collectAsState()

    // Kiểm tra nếu phim đã được đánh giá
    LaunchedEffect(booking.movieId) {
        reviewViewModel.checkIfMovieRated(booking.movieId)
        if (isRated) {
            reviewViewModel.getUserReviewForMovie(booking.movieId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin chi tiết") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mã barcode (chuyển lên đầu)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = booking.movieTitle.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    )

                    // Tạo mã barcode từ booking ID
                    val barcodeBitmap = generateBarcode(booking.id, 800, 200)
                    if (barcodeBitmap != null) {
                        Image(
                            bitmap = barcodeBitmap.asImageBitmap(),
                            contentDescription = "Barcode",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(horizontal = 8.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Trong Card hiển thị mã barcode:
                    Text(
                        text = "Mã vé: ${booking.id}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Text(
                        text = "Vui lòng xuất trình mã vé này khi đến rạp để nhận vé. Mã vé chỉ có hiệu lực duy nhất 1 lần.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card thông tin vé
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // ... giữ nguyên phần nội dung thông tin vé ...
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Poster phim
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(booking.movieImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Movie poster",
                            placeholder = painterResource(id = R.drawable.placeholder_image),
                            error = painterResource(id = R.drawable.placeholder_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Thông tin phim
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = booking.movieTitle.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // ... giữ nguyên các thông tin khác của phim ...
                            // Rạp phim
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_theater),
                                    contentDescription = null,
                                    tint = primaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = booking.theaterName.ifEmpty { "Chưa có thông tin rạp" },
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Địa chỉ
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Địa chỉ rạp",
                                    tint = primaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = booking.theaterLocation.ifEmpty { "Không có địa chỉ" },
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Phòng chiếu
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_screen),
                                    contentDescription = null,
                                    tint = primaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Phòng ${booking.roomName}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ngày & giờ chiếu
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = primaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = booking.showDate,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_clock),
                                    contentDescription = null,
                                    tint = primaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = booking.showTime,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ghế ngồi
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EventSeat,
                                    contentDescription = null,
                                    tint = primaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val seatText = if (booking.seats.isNotEmpty()) {
                                    booking.seats.joinToString(", ")
                                } else {
                                    "Không có thông tin ghế"
                                }
                                Text(
                                    text = seatText,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Giá & trạng thái
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tổng thanh toán", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_payment),
                                    contentDescription = null,
                                    tint = primaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${currencyFormat.format(booking.totalPrice)} đ",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryRed
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Trạng thái", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))

                            if (isPastBooking) {
                                // Nếu là phim đã xem, hiển thị trạng thái đánh giá
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isRated) Color(0xFF9E9E9E) else Color(0xFF4CAF50),
                                    modifier = Modifier.clickable(enabled = !isRated) {
                                        if (!isRated) {
                                            onRateMovieClick(booking)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (isRated) "Đã đánh giá" else "Đánh giá phim",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                }
                            } else {
                                // Hiển thị trạng thái thanh toán cho phim sắp xem
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF4CAF50)
                                ) {
                                    Text(
                                        text = booking.status,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Hiển thị thông tin đánh giá nếu đã đánh giá
            if (isRated && isPastBooking && currentUserReview != null) {
                Spacer(modifier = Modifier.height(16.dp))

                // Thêm Card hiển thị đánh giá người dùng
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nhận xét của bạn",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = primaryRed
                            )

                            // Nút chỉnh sửa đánh giá
                            IconButton(
                                onClick = { onRateMovieClick(booking) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Chỉnh sửa đánh giá",
                                    tint = primaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hiển thị số sao
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hiển thị các icon sao dựa trên đánh giá
                            Row {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= currentUserReview!!.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (i <= currentUserReview!!.rating) Color(0xFFFFD700) else Color.LightGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = when (currentUserReview!!.rating) {
                                    1 -> "(Tệ)"
                                    2 -> "(Không hay)"
                                    3 -> "(Bình thường)"
                                    4 -> "(Hay)"
                                    5 -> "(Rất hay)"
                                    else -> ""
                                },
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }

                        // Nếu có nội dung đánh giá
                        if (currentUserReview!!.comment.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Text(
                                    text = currentUserReview!!.comment,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(16.dp),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// Hàm tạo barcode
private fun generateBarcode(content: String, width: Int, height: Int): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.CODE_128,
            width,
            height
        )

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


@Composable
fun ReviewCard(
    review: Review,
    primaryRed: Color,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đánh giá của bạn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primaryRed
                )

                // Nút chỉnh sửa đánh giá
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa đánh giá",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hiển thị số sao
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Số sao: ",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= review.rating) Color(0xFFFFD700) else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when(review.rating) {
                        1 -> "(Tệ)"
                        2 -> "(Không hay)"
                        3 -> "(Bình thường)"
                        4 -> "(Hay)"
                        5 -> "(Rất hay)"
                        else -> ""
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Ngày đánh giá
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
                Text(
                    text = "Đánh giá ngày: ${dateFormatter.format(review.reviewDate)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Nếu có nội dung đánh giá
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Divider(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Nhận xét của bạn:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Text(
                        text = review.comment,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
