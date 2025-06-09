package com.example.movie_booking_app.ui.screens.Booking

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.movie_booking_app.data.model.TheaterWithShowtimes
import com.example.movie_booking_app.data.model.Showtime
import com.example.movie_booking_app.ui.components.WeeklyCalendar
import com.example.movie_booking_app.ui.viewmodel.SeatSelectionViewModel
import com.example.movie_booking_app.viewmodel.BookingViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController,
    movieId: String,
    movieTitle: String,
    viewModel: BookingViewModel = viewModel(),
    seatViewModel: SeatSelectionViewModel = viewModel()
) {
    val availableDates by viewModel.availableDates.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val theaters by viewModel.theaters.collectAsState()
    val expandedTheaterId by viewModel.expandedTheaterId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(movieId) {
        if (movieId.isBlank()) {
            Log.e("BookingScreen", "Lỗi: MovieId trống!")
            navController.popBackStack()
            return@LaunchedEffect
        }
        viewModel.loadDataForMovie(movieId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đặt vé: $movieTitle",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
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
                    color = Color(0xFFE71A0F)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (availableDates.isNotEmpty()) {
                        WeeklyCalendar(
                            availableDates = availableDates,
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                viewModel.selectDate(date)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        )
                    }
                    // Hiển thị danh sách rạp
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        if (theaters.isEmpty()) {
                            item {
                                Text(
                                    text = "Không có suất chiếu ",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(theaters) { theater ->
                                TheaterItem(
                                    theater = theater,
                                    isExpanded = expandedTheaterId == theater.theaterId,
                                    onTheaterClick = {
                                        viewModel.toggleTheaterExpansion(theater.theaterId)
                                    },
                                    onShowtimeSelected = { showtime ->
                                        // Lưu thông tin rạp vào SeatSelectionViewModel
                                        seatViewModel.setTheaterInfo(
                                            name = theater.name,
                                            address = theater.location
                                        )
                                        val encodedTitle = movieTitle.replace(" ", "_")
                                        navController.navigate("seat-selection/${showtime.scheduleId}/$encodedTitle")
                                    }
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
fun TheaterItem(
    theater: TheaterWithShowtimes,
    isExpanded: Boolean,
    onTheaterClick: () -> Unit,
    onShowtimeSelected: (Showtime) -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale("vi"))
    val visibleState = remember { MutableTransitionState(false) }

    visibleState.targetState = isExpanded

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Phần header của rạp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTheaterClick() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = theater.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = theater.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                    tint = Color(0xFFE71A0F)
                )
            }

            // Phần mở rộng hiển thị suất chiếu
            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Suất chiếu:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        theater.showtimes.forEach { showtime ->
                            Button(
                                onClick = { onShowtimeSelected(showtime) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(timeFormat.format(showtime.startTime))
                            }
                        }
                    }
                }
            }
        }
    }
}