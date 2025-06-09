package com.example.movie_booking_app.ui.screens.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movie_booking_app.R
import com.example.movie_booking_app.data.model.Booking
import com.example.movie_booking_app.viewmodel.AuthViewModel
import com.example.movie_booking_app.viewmodel.BookingViewModel
import com.example.movie_booking_app.viewmodel.ReviewViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private const val TAG = "MyTicketsScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun MyTicketsScreen(
    authViewModel: AuthViewModel,
    bookingViewModel: BookingViewModel,
    reviewViewModel: ReviewViewModel, // Thêm reviewViewModel
    onBackClick: () -> Unit,
    onTicketClick: (Booking) -> Unit,
    onRateMovieClick: (Booking) -> Unit
) {
    val upcomingBookings by bookingViewModel.upcomingBookings.collectAsState()
    val pastBookings by bookingViewModel.pastBookings.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val error by bookingViewModel.error.collectAsState()

    val primaryRed = Color(0xFFE71A0F)
    // Lấy danh sách phim đã đánh giá
    val ratedMovieIds by reviewViewModel.ratedMovieIds.collectAsState()

    // Khi màn hình được hiển thị, kiểm tra các phim đã đánh giá
    LaunchedEffect(pastBookings) {
        // Với mỗi booking quá khứ, kiểm tra xem đã đánh giá chưa
        pastBookings.forEach { booking ->
            reviewViewModel.checkIfMovieRated(booking.movieId)
        }
    }
    // Thêm một số logs để kiểm tra dữ liệu
    LaunchedEffect(Unit) {
        Log.d(TAG, "LaunchedEffect: Gọi getUserBookings()")
        bookingViewModel.getUserBookings()
    }

    // Log thông tin về số lượng vé khi có sự thay đổi
    LaunchedEffect(upcomingBookings, pastBookings, isLoading, error) {
        Log.d(TAG, "State updated: upcomingBookings=${upcomingBookings.size}, pastBookings=${pastBookings.size}, isLoading=$isLoading, error=$error")
    }

    // Pager state và scope
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("Phim sắp xem", "Phim đã xem")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vé của tôi") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = primaryRed,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        height = 2.dp,
                        color = primaryRed
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }

            // Content
            HorizontalPager(
                count = tabs.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = primaryRed
                            )
                        }
                        error != null -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Đã xảy ra lỗi: $error",
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        Log.d(TAG, "Retry button clicked")
                                        bookingViewModel.getUserBookings()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
                                ) {
                                    Text("Thử lại")
                                }
                            }
                        }
                        else -> {
                            val bookingsToShow = if (page == 0) upcomingBookings else pastBookings
                            val isPastBookings = page == 1  // Tab 1 là "Phim đã xem"

                            Log.d(TAG, "Hiển thị ${bookingsToShow.size} vé ở tab $page")

                            if (bookingsToShow.isEmpty()) {
                                EmptyTicketsView("Không có vé nào")
                            } else {
                                TicketsList(
                                    bookings = bookingsToShow,
                                    onTicketClick = { booking ->
                                        Log.d("MyTicketsScreen", "Clicked on ticket with ID: ${booking.id}")
                                        onTicketClick(booking)
                                    },
                                    isPastBookings = isPastBookings,
                                    ratedMovieIds = ratedMovieIds, // Truyền danh sách phim đã đánh giá
                                    onRateMovieClick = onRateMovieClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTicketsView(message: String = "Không có vé nào") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_tickets),
            contentDescription = "No tickets",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TicketsList(
    bookings: List<Booking>,
    onTicketClick: (Booking) -> Unit,
    isPastBookings: Boolean = false,
    ratedMovieIds: Set<String> = emptySet(), // Thêm tham số này
    onRateMovieClick: (Booking) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(bookings) { booking ->
            TicketItem(
                booking = booking,
                onClick = { onTicketClick(booking) },
                isPastBooking = isPastBookings,
                isRated = ratedMovieIds.contains(booking.movieId), // Kiểm tra phim đã được đánh giá chưa
                onRateMovieClick = onRateMovieClick
            )
        }
    }
}

@Composable
fun TicketItem(
    booking: Booking,
    onClick: () -> Unit,
    isPastBooking: Boolean = false,
    isRated: Boolean = false, // Thêm tham số này
    onRateMovieClick: (Booking) -> Unit = {}
) {
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val primaryRed = Color(0xFFE71A0F)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Khi click vào phần vé (ngoại trừ nút đánh giá) thì vào chi tiết phim
                Log.d("MyTicketsScreen", "Card clicked for movie: ${booking.movieTitle}")
                onClick()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Movie info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Movie poster
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(booking.movieImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Movie poster",
                    modifier = Modifier
                        .width(70.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(id = R.drawable.placeholder_poster),
                    error = painterResource(id = R.drawable.placeholder_poster)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Movie details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = booking.movieTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Show date and time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = primaryRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = booking.showDate,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = primaryRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = booking.showTime,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }

//                    // Room info
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Default.LocationOn,
//                            contentDescription = null,
//                            tint = primaryRed,
//                            modifier = Modifier.size(14.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "Phòng ${booking.roomName}",
//                            fontSize = 12.sp,
//                            color = Color.DarkGray
//                        )
//                    }

                    // Seats info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ghế: ${booking.seats.joinToString(", ")}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = Color.LightGray
            )

            // Total price and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = primaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${currencyFormat.format(booking.totalPrice)} đ",
                        fontWeight = FontWeight.Bold,
                        color = primaryRed
                    )
                }

                // Phần thay đổi ở đây: Hiển thị nút "Đánh giá phim" thay vì trạng thái "Đã thanh toán"
                if (isPastBooking) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isRated) Color(0xFF9E9E9E) else Color(0xFF4CAF50), // Màu khác nếu đã đánh giá
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = !isRated) { // Không cho phép click nếu đã đánh giá
                                if (!isRated) {
                                    Log.d("MyTicketsScreen", "Rate movie clicked: ${booking.movieTitle}")
                                    onRateMovieClick(booking)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isRated) "Đã đánh giá" else "Đánh giá phim",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Trạng thái bình thường cho phim sắp xem
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (booking.status == "Đã thanh toán") Color(0xFF4CAF50) else Color(0xFFFFA000),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = booking.status,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
