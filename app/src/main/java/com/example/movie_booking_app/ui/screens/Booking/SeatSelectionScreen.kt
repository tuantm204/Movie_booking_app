package com.example.movie_booking_app.ui.screens.Booking

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.Seat
import com.example.movie_booking_app.data.model.SeatStatus
import com.example.movie_booking_app.data.model.SeatType
import com.example.movie_booking_app.ui.theme.*
import com.example.movie_booking_app.ui.viewmodel.SeatSelectionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    navController: NavController,
    showtimeId: String,
    movieTitle: String,
    viewModel: SeatSelectionViewModel = viewModel()
) {
    // Tải dữ liệu khi màn hình được khởi tạo
    LaunchedEffect(key1 = showtimeId) {
        viewModel.loadData(showtimeId)
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val seatMatrix by viewModel.seatMatrix.collectAsState()
    val selectedSeats by viewModel.selectedSeats.collectAsState()
    val showtime by viewModel.showtime.collectAsState()
    val movie by viewModel.movie.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()

    val currencyFormat = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        movie?.imagelink?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        )
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = movieTitle.uppercase(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
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
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                } else if (seatMatrix.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Không thể tải thông tin ghế",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Red700)
                        ) {
                            Text("Quay lại")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            showtime?.let {
                                Text(
                                    text = "${SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(it.startTime)} | ${it.roomName}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            // Màn hình
                            Text(
                                text = "MÀN HÌNH",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color.Gray.copy(alpha = 0.5f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(vertical = 6.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        // Phần giữa: Ma trận ghế
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            // Ma trận ghế
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                seatMatrix.forEach { row ->
                                    SeatRow(row = row, onSeatClick = { seat ->
                                        viewModel.toggleSeatSelection(seat)
                                    })
                                }
                            }
                        }


                        // Phần dưới: Chú thích và thông tin đặt vé
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SeatLegend(color = Gray200, text = "Đã đặt", textColor = Color.White)
                                SeatLegend(color = Blue500, text = "Đang chọn", textColor = Color.White)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SeatLegend(color = Green500, text = "Thường", textColor = Color.White)
                                SeatLegend(color = Orange500, text = "VIP", textColor = Color.White)
                                SeatLegend(color = Purple500, text = "Double", textColor = Color.White)
                            }

                            // Phần thông tin đặt vé
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = movieTitle.uppercase(),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.Black
                                            )

                                            Text(
                                                text = movie?.language ?: "Tiếng Việt - Phụ đề Tiếng Anh",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            if (selectedSeats.isNotEmpty()) {
                                                Text(
                                                    text = "${selectedSeats.size} ghế",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.DarkGray
                                                )
                                            }

                                            val priceText = if (selectedSeats.isEmpty()) "Chọn ghế"
                                            else "${currencyFormat.format(totalPrice)} đ"

                                            Text(
                                                text = priceText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.Black
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.saveSelectedSeats()
                                            val encodedMovieTitle = movieTitle.replace(" ", "_")
                                            navController.navigate("order-summary/$showtimeId/$encodedMovieTitle")
                                        },
                                        enabled = selectedSeats.isNotEmpty(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Red700,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("ĐẶT VÉ", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeatRow(
    row: List<Seat>,
    onSeatClick: (Seat) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        val filteredRow = row.filter { seat ->
            !(seat.row == "F" && seat.number == "7")
        }
        if (filteredRow.isNotEmpty()) {
            Text(
                text = filteredRow.first().row,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp)
                    .width(15.dp),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        filteredRow.forEach { seat ->
            EnhancedSeatItem(
                seat = seat,
                isDoubleWidth = seat.type == SeatType.DOUBLE,
                onClick = { onSeatClick(seat) }
            )
        }
    }
}

@Composable
fun EnhancedSeatItem(
    seat: Seat,
    isDoubleWidth: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            seat.status == SeatStatus.BOOKED -> Gray200
            seat.status == SeatStatus.SELECTED -> Blue500
            seat.type == SeatType.STANDARD -> Green500
            seat.type == SeatType.VIP -> Orange500
            seat.type == SeatType.DOUBLE -> Purple500
            else -> Color.LightGray
        },
        animationSpec = tween(300),
        label = "backgroundColorAnimation"
    )

    val elevation by animateDpAsState(
        targetValue = if (seat.status == SeatStatus.SELECTED) 6.dp else 2.dp,
        animationSpec = tween(300),
        label = "elevationAnimation"
    )
    val width = if (isDoubleWidth) 58.dp else 26.dp
    val height = 26.dp

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(4.dp),
                clip = false
            )
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(enabled = seat.status != SeatStatus.BOOKED) { onClick() }
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seat.id,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = if (seat.status == SeatStatus.SELECTED) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SeatLegend(
    color: Color,
    text: String,
    textColor: Color = Color.Black
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .shadow(2.dp, RoundedCornerShape(2.dp))
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(text, fontSize = 12.sp, color = textColor)
    }
}