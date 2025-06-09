package com.example.movie_booking_app.ui.navigation

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movie_booking_app.viewmodel.AuthViewModel
import com.example.movie_booking_app.viewmodel.MovieViewModel
import com.example.movie_booking_app.viewmodel.NewsViewModel
import com.example.movie_booking_app.viewmodel.BookingViewModel
import com.example.movie_booking_app.viewmodel.ReviewViewModel
import com.example.movie_booking_app.ui.components.AllNewsScreen
import com.example.movie_booking_app.ui.components.AllVideosScreen
import com.example.movie_booking_app.ui.screens.Auth.LoginScreen
import com.example.movie_booking_app.ui.screens.Auth.RegisterScreen
import com.example.movie_booking_app.ui.screens.Booking.BookingScreen
import com.example.movie_booking_app.ui.screens.Booking.OrderSummaryScreen
import com.example.movie_booking_app.ui.screens.Booking.SeatSelectionScreen
import com.example.movie_booking_app.ui.screens.Booking.TicketConfirmationScreen
import com.example.movie_booking_app.ui.screens.Home.Home
import com.example.movie_booking_app.ui.screens.Movie.MovieDetailScreen
import com.example.movie_booking_app.ui.screens.News.NewsDetailScreen
import com.example.movie_booking_app.ui.screens.Profile.ChangePasswordScreen
import com.example.movie_booking_app.ui.screens.Profile.PersonalInfoScreen
import com.example.movie_booking_app.ui.screens.Profile.ProfileScreen
import com.example.movie_booking_app.ui.viewmodel.SeatSelectionViewModel
import com.example.movie_booking_app.ui.screens.Profile.MyTicketsScreen
import com.example.movie_booking_app.ui.screens.Profile.TicketDetailScreen
import com.example.movie_booking_app.ui.screens.Rating.MovieRatingScreen
import com.example.movie_booking_app.data.repository.AuthState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

//Định nghĩa các màn hình trong ứng dụng
sealed class Screen(val route: String) {
    object MovieList : Screen("movieList")
    object MovieDetail : Screen("movieDetail/{movieId}")
    object NewsDetail : Screen("news_detail")
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object AllNews : Screen("all_news")
    object AllVideos : Screen("all_videos")
    object AllVideosWithParam : Screen("all_videos/{videoToPlay}")
    object MyTickets : Screen("MyTictkets")

    fun createRoute(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                route.replace("{$arg}", arg)
            }
        }
    }
}

//Quản lý điều hướng trong ứng dụng và tích hợp Firebase Authentication
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val movieViewModel: MovieViewModel = viewModel()
    val newsViewModel: NewsViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val seatSelectionViewModel: SeatSelectionViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    val reviewViewModel: ReviewViewModel = viewModel()

    // Theo dõi trạng thái đăng nhập để điều hướng phù hợp
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("zalo_pay", android.content.Context.MODE_PRIVATE)
        val shouldRedirect = prefs.getBoolean("should_redirect_to_confirmation", false)
        if (shouldRedirect) {
            val bookingId = prefs.getString("last_booking_id", null)
            if (bookingId != null) {
                prefs.edit().putBoolean("should_redirect_to_confirmation", false).apply()
                navController.navigate("ticket-confirmation/$bookingId") {
                    popUpTo(0) { inclusive = false }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.MovieList.route
    ) {
        // 1. ROUTE MÀN HÌNH CHÍNH - Hiển thị danh sách phim, tin tức và các mục menu chính
        composable(Screen.MovieList.route) {
            Home(
                movieViewModel = movieViewModel,
                newsViewModel = newsViewModel,
                authViewModel = authViewModel,
                onMovieClick = { movie ->
                    val movieId = movie.title ?: "unknown"
                    navController.navigate("movieDetail/$movieId")
                },
                onNewsClick = { news ->
                    newsViewModel.selectNews(news)
                    navController.navigate(Screen.NewsDetail.route)
                },
                onAllNewsClick = {
                    navController.navigate(Screen.AllNews.route)
                },
                onVideoClick = { movie ->
                    if (!movie.trailer.isNullOrEmpty()) {
                        navController.navigate("all_videos/${movie.title}")
                    }
                },
                onAllVideosClick = {
                    navController.navigate(Screen.AllVideos.route)
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onTicketsClick = {
                    if (authViewModel.authState.value is AuthState.Authenticated) {
                        navController.navigate("my_tickets")
                    } else {
                        navController.navigate("login")
                    }
                }
            )
        }

        // 2. ROUTE CHI TIẾT PHIM - Hiển thị thông tin chi tiết về bộ phim được chọn
        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            val movie = movieViewModel.getMovieById(movieId)

            if (movie != null) {
                MovieDetailScreen(
                    movie = movie,
                    onBackClick = { navController.popBackStack() },
                    onBookClick = {
                        if (authState is AuthState.Authenticated) {
                            val encodedTitle = movie.title?.replace(" ", "_") ?: movie.id
                            navController.navigate("booking/${movie.id}/$encodedTitle")
                        } else {
                            navController.navigate(Screen.Login.route)
                        }
                    },
                    movieViewModel = movieViewModel,
                    newsViewModel = newsViewModel,
                    reviewViewModel = reviewViewModel,
                    onNewsClick = { news ->
                        newsViewModel.selectNews(news)
                        navController.navigate(Screen.NewsDetail.route)
                    },
                    onViewAllNewsClick = {
                        navController.navigate(Screen.AllNews.route)
                    }
                )
            }
        }

        // 3. ROUTE CHI TIẾT TIN TỨC - Hiển thị nội dung đầy đủ của tin tức được chọn
        composable(Screen.NewsDetail.route) {
            val selectedNews = newsViewModel.selectedNews.collectAsState().value // Thay đổi
            if (selectedNews != null) {
                NewsDetailScreen(
                    news = selectedNews,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(key1 = Unit) {
                    navController.popBackStack()
                }
            }
        }

        // 4. ROUTE TẤT CẢ TIN TỨC - Hiển thị danh sách đầy đủ các tin tức
        composable(Screen.AllNews.route) {
            AllNewsScreen(
                viewModel = newsViewModel,
                onBackClick = { navController.popBackStack() },
                onNewsClick = { news ->
                    newsViewModel.selectNews(news)
                    navController.navigate(Screen.NewsDetail.route)
                }
            )
        }

        // 5. ROUTE TẤT CẢ VIDEO - Hiển thị danh sách các video trailer phim
        composable(Screen.AllVideos.route) {
            AllVideosScreen(
                viewModel = movieViewModel,
                onBackClick = { navController.popBackStack() },
                onVideoClick = { movie -> },
                initialVideoId = null
            )
        }

        // 6. ROUTE TẤT CẢ VIDEO VỚI PARAM - Hiển thị video trailer với một video được tự động phát
        composable(
            route = Screen.AllVideosWithParam.route,
            arguments = listOf(
                navArgument("videoToPlay") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoToPlay = backStackEntry.arguments?.getString("videoToPlay") ?: ""
            AllVideosScreen(
                viewModel = movieViewModel,
                onBackClick = { navController.popBackStack() },
                onVideoClick = { movie -> },
                initialVideoId = videoToPlay
            )
        }

        // 7. ROUTE ĐĂNG NHẬP - Cho phép người dùng đăng nhập vào hệ thống
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.popBackStack()
                },
                onForgotPasswordClick = {
                    // navController.navigate("forgot_password")
                }
            )
        }

        // 8. ROUTE ĐĂNG KÝ - Cho phép người dùng tạo tài khoản mới
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // 9. ROUTE HỒ SƠ NGƯỜI DÙNG - Hiển thị thông tin tài khoản và các tùy chọn quản lý
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onPersonalInfoClick = { navController.navigate("personal_info") },
                onChangePasswordClick = { navController.navigate("change_password") },
                onTransactionHistoryClick = { navController.navigate("my_tickets") },
                onLogoutClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // 10. ROUTE THÔNG TIN CÁ NHÂN - Cho phép người dùng xem và cập nhật thông tin cá nhân
        composable("personal_info") {
            PersonalInfoScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 11. ROUTE ĐỔI MẬT KHẨU - Cho phép người dùng thay đổi mật khẩu hiện tại
        composable("change_password") {
            ChangePasswordScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 12. ROUTE ĐẶT VÉ - Cho phép người dùng chọn lịch chiếu cho phim đã chọn
        composable(
            route = "booking/{movieId}/{movieTitle}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType },
                navArgument("movieTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            val movieTitle = backStackEntry.arguments?.getString("movieTitle")?.replace("_", " ") ?: ""
            BookingScreen(
                navController = navController,
                movieId = movieId,
                movieTitle = movieTitle,
                seatViewModel = seatSelectionViewModel
            )
        }

        // 13. ROUTE CHỌN GHẾ (Cũ, không sử dụng) - Giữ lại cho khả năng tương thích
        composable(
            route = "seatSelection/{scheduleId}",
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: ""
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Màn hình chọn ghế - Suất chiếu: $scheduleId")
            }
        }


        // 14. ROUTE CHỌN GHẾ - Cho phép người dùng chọn ghế ngồi cho suất chiếu đã chọn
        composable(
            "seat-selection/{showtimeId}/{movieTitle}"
        ) { backStackEntry ->
            val showtimeId = backStackEntry.arguments?.getString("showtimeId") ?: ""
            val encodedMovieTitle = backStackEntry.arguments?.getString("movieTitle") ?: ""
            val movieTitle = encodedMovieTitle.replace("_", " ")
            SeatSelectionScreen(
                navController = navController,
                showtimeId = showtimeId,
                movieTitle = movieTitle,
                viewModel = seatSelectionViewModel
            )
        }

        // 15. ROUTE TÓM TẮT ĐƠN HÀNG - Hiển thị thông tin tóm tắt đặt vé và xử lý thanh toán
        composable(
            "order-summary/{showtimeId}/{movieTitle}"
        ) { backStackEntry ->
            val showtimeId = backStackEntry.arguments?.getString("showtimeId") ?: ""
            val encodedMovieTitle = backStackEntry.arguments?.getString("movieTitle") ?: ""
            val movieTitle = encodedMovieTitle.replace("_", " ")
            OrderSummaryScreen(
                navController = navController,
                showtimeId = showtimeId,
                movieTitle = movieTitle,
                viewModel = seatSelectionViewModel
            )
        }

        // 16. ROUTE XÁC NHẬN VÉ - Hiển thị thông tin vé sau khi đặt hàng thành công
        composable("ticket-confirmation/{bookingId}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            TicketConfirmationScreen(
                navController = navController,
                bookingId = bookingId
            )
        }

        // 17. ROUTE DANH SÁCH VÉ CỦA TÔI - Hiển thị tất cả các vé đã đặt của người dùng
        composable("my_tickets") {
            MyTicketsScreen(
                authViewModel = authViewModel,
                bookingViewModel = bookingViewModel,
                reviewViewModel = reviewViewModel, // Thêm reviewViewModel
                onBackClick = { navController.popBackStack() },
                onTicketClick = { booking ->
                    navController.navigate("ticket_detail/${booking.id}")
                },
                onRateMovieClick = { booking ->
                    navController.navigate("movie_rating/${booking.id}")
                }
            )
        }

        // 18. ROUTE CHI TIẾT VÉ - Hiển thị thông tin chi tiết của một vé cụ thể
        composable(
            route = "ticket_detail/{bookingId}",
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            // Tải dữ liệu booking từ BookingViewModel
            val allBookings = bookingViewModel.userBookings.collectAsState().value +
                    bookingViewModel.pastBookings.collectAsState().value +
                    bookingViewModel.upcomingBookings.collectAsState().value
            val booking = allBookings.find { it.id == bookingId }
            if (booking != null) {
                // Xác định xem booking này là phim đã xem hay chưa
                val isPastBooking = bookingViewModel.pastBookings.collectAsState().value.any { it.id == bookingId }
                TicketDetailScreen(
                    booking = booking,
                    reviewViewModel = reviewViewModel,
                    isPastBooking = isPastBooking,
                    onBackClick = { navController.popBackStack() },
                    onRateMovieClick = { selectedBooking ->
                        navController.navigate("movie_rating/${selectedBooking.id}")
                    }
                )
            } else {
                // Hiển thị trạng thái loading hoặc error
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE71A0F))
                }
            }
        }
        // Thay đổi route cho MovieRatingScreen
        composable(
            route = "movie_rating/{bookingId}",
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            // Tải dữ liệu trực tiếp trong màn hình MovieRatingScreen
            MovieRatingScreen(
                bookingId = bookingId,
                reviewViewModel = reviewViewModel,
                bookingViewModel = bookingViewModel, // Truyền thêm bookingViewModel để lấy thông tin booking
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}