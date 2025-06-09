package com.example.movie_booking_app.ui.screens.Booking

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movie_booking_app.ui.theme.Red700
import com.example.movie_booking_app.ui.viewmodel.SeatSelectionViewModel
import com.example.movie_booking_app.utils.ZaloPayUtils
import com.example.movie_booking_app.viewmodel.BookingViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryScreen(
    navController: NavController,
    showtimeId: String,
    movieTitle: String,
    viewModel: SeatSelectionViewModel = viewModel()

) {
    LaunchedEffect(key1 = showtimeId) {
        viewModel.loadData(showtimeId)
    }
    val bookingViewModel: BookingViewModel = viewModel()

    val selectedSeats by viewModel.selectedSeats.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val showtime by viewModel.showtime.collectAsState()
    val movie by viewModel.movie.collectAsState()

    // State for the UI
    var mbavoucherExpanded by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf(0) } // 0 = ATM card
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    var isPaying by remember { mutableStateOf(false) }

    // Các định dạng
    val currencyFormat = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }
    val dateFormat = remember { SimpleDateFormat("EEEE, dd 'tháng' M, yyyy", Locale("vi")) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Animation cho mũi tên dropdown
    val arrowRotation by animateFloatAsState(
        targetValue = if (mbavoucherExpanded) 180f else 0f,
        label = "arrowRotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Thanh toán") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            // PHẦN THÔNG TIN PHIM
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Thông tin phim
                    movie?.let { currentMovie ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Poster phim
                            currentMovie.imagelink.let { imageUrl ->
                                if (imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Poster phim",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Thông tin phim
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = movieTitle.uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start
                                )

                                Text(
                                    text = currentMovie.rated.ifEmpty {
                                        "P - Phim được phép phổ biến đến người xem 0 mọi lứa tuổi"
                                    },
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                // Thông tin suất chiếu
                                showtime?.let { currentShowtime ->
                                    val date = dateFormat.format(currentShowtime.startTime)
                                    val startTime = timeFormat.format(currentShowtime.startTime)
                                    val endTime = timeFormat.format(currentShowtime.endTime)

                                    Text(
                                        text = "Ngày chiếu: $date",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "Thời gian: $startTime ~ $endTime",
                                        fontSize = 14.sp
                                    )

                                    Text(
                                        text = "Phòng chiếu: ${currentShowtime.roomName}",
                                        fontSize = 14.sp
                                    )

                                    Text(
                                        text = "Ghế: ${selectedSeats.joinToString { seat -> seat.id }}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Red700,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tổng thanh toán
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Red700)
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Tổng Thanh Toán: ${currencyFormat.format(totalPrice)} đ",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            // Phần nội dung có thể cuộn
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // PHẦN THÔNG TIN VÉ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(0.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "THÔNG TIN VÉ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Số lượng",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${selectedSeats.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tổng",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${currencyFormat.format(totalPrice)} đ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                    shape = RoundedCornerShape(0.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { mbavoucherExpanded = !mbavoucherExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MBA VOUCHER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )

                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Mở rộng",
                                modifier = Modifier.rotate(arrowRotation)
                            )
                        }

                        // Phần mở rộng khi bấm vào
                        AnimatedVisibility(visible = mbavoucherExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = "Không có voucher khả dụng",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // PHẦN TỔNG KẾT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                    shape = RoundedCornerShape(0.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "TỔNG KẾT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tổng cộng",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${currencyFormat.format(totalPrice)} đ",
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Giảm giá",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "0 đ",
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Thẻ quà tặng/ eGift",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "0 đ",
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Còn lại",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${currencyFormat.format(totalPrice)} đ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // PHẦN THANH TOÁN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "THANH TOÁN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                // Phương thức thanh toán
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(0.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Các phương thức thanh toán giữ nguyên...
                        SelectablePaymentMethod("ATM card (Thẻ nội địa)", 0, selectedPaymentMethod) {
                            selectedPaymentMethod = 0
                        }
                        SelectablePaymentMethod("Thẻ quốc tế (Visa, Master, Amex, JCB)", 1, selectedPaymentMethod) {
                            selectedPaymentMethod = 1
                        }
                        SelectablePaymentMethod("Momo (Mã MMCGV - 5K)", 2, selectedPaymentMethod) {
                            selectedPaymentMethod = 2
                        }
                        SelectablePaymentMethod("Zalo pay (Đồng giá 84K + combo quà đến 675K)", 3, selectedPaymentMethod) {
                            selectedPaymentMethod = 3
                        }
                        SelectablePaymentMethod("VN pay (Nhập VNPAYCGV giảm đến 10K/bill)", 4, selectedPaymentMethod) {
                            selectedPaymentMethod = 4
                        }
                        SelectablePaymentMethod("ShopeePay (Giảm 5k)", 5, selectedPaymentMethod) {
                            selectedPaymentMethod = 5
                        }
                        SelectablePaymentMethod("Thanh toán bằng Apple Pay", 6, selectedPaymentMethod) {
                            selectedPaymentMethod = 6
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Red700,
                            uncheckedColor = Color.Gray
                        )
                    )

                    Text(
                        text = buildAnnotatedString {
                            append("Tôi đồng ý với ")
                            pushStyle(SpanStyle(color = Red700, textDecoration = TextDecoration.Underline))
                            append("điều khoản sử dụng")
                            pop()
                            append(" và đang mua vé cho người có độ tuổi phù hợp với từng loại vé. ")
                            pushStyle(SpanStyle(color = Red700, textDecoration = TextDecoration.Underline))
                            append("Chi tiết xem tại đây!")
                            pop()
                        },
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        viewModel.setTotalPrice(totalPrice)
                        if (selectedPaymentMethod == 3) {
                            if (isPaying) return@Button
                            isPaying = true
                            coroutineScope.launch {
                                try {
                                    val orderResult = ZaloPayUtils.createOrder(totalPrice)
                                    if (orderResult != null && orderResult.optString("returncode") == "1") {
                                        val token = orderResult.optString("zptranstoken")
                                        // Tạo mã booking và lưu thông tin
                                        viewModel.generateBookingCode()
                                        val bookingId = viewModel.bookingCode.value
                                        // Đảm bảo đã set tổng tiền
                                        viewModel.setTotalPrice(totalPrice)
                                        // Lưu thông tin vào SharedPreferences
                                        val sharedPrefs = context.getSharedPreferences("zalo_pay", Context.MODE_PRIVATE)
                                        sharedPrefs.edit()
                                            .putString("last_booking_id", bookingId)
                                            .putBoolean("should_redirect_to_confirmation", true)
                                            .apply()
                                        activity?.let {
                                            // Thay đổi trong callback ZaloPay khi thanh toán thành công
                                            ZaloPayUtils.payOrder(it, token) { success, error ->
                                                isPaying = false
                                                if (success) {
                                                    viewModel.saveBookingToFirestore()
                                                    // Hiện toast thông báo
                                                    Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
                                                    // Chuyển đến màn hình xác nhận vé với bookingId
                                                    val bookingId = viewModel.bookingCode.value
                                                    // Sử dụng bookingViewModel đã được khởi tạo trước đó
                                                    bookingViewModel.getUserBookings()
                                                    // Điều hướng sau khi lưu booking thành công
                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        navController.navigate("ticket-confirmation/$bookingId") {
                                                            popUpTo("seat-selection/$showtimeId/${movieTitle.replace(" ", "_")}") {
                                                                inclusive = true
                                                            }
                                                        }
                                                    }, 1000)
                                                } else {
                                                }
                                            }
                                        } ?: run {
                                            isPaying = false
                                            Toast.makeText(context, "Không thể khởi tạo thanh toán", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        isPaying = false
                                        val errorMsg = orderResult?.optString("returnmessage") ?: "Không tạo được đơn hàng ZaloPay"
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    isPaying = false
                                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            viewModel.generateBookingCode()
                            navController.navigate("ticket-confirmation/$showtimeId") {
                                popUpTo("seat-selection/$showtimeId/${movieTitle.replace(" ", "_")}") {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red700),
                    shape = RoundedCornerShape(4.dp),
                    enabled = termsAccepted && !isPaying
                ) {
                    Text(
                        text = if (selectedPaymentMethod == 3 && isPaying) "Đang chuyển sang ZaloPay..." else "Tôi đồng ý và Tiếp tục",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SelectablePaymentMethod(
    name: String,
    id: Int,
    selectedId: Int,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        if (id == selectedId) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Red700
            )
        }
    }
}