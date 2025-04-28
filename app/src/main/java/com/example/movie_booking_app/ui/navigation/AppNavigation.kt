package com.example.movie_booking_app.ui.navigation

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
import com.example.movie_booking_app.data.repository.AuthState
import com.example.movie_booking_app.data.repository.AuthViewModel
import com.example.movie_booking_app.data.repository.MovieViewModel
import com.example.movie_booking_app.data.repository.NewsViewModel
import com.example.movie_booking_app.ui.components.AllNewsScreen
import com.example.movie_booking_app.ui.components.AllVideosScreen
import com.example.movie_booking_app.ui.screens.Auth.LoginScreen
import com.example.movie_booking_app.ui.screens.Auth.RegisterScreen
import com.example.movie_booking_app.ui.screens.Home.Home
import com.example.movie_booking_app.ui.screens.MovieDetailScreen
import com.example.movie_booking_app.ui.screens.News.NewsDetailScreen
import com.example.movie_booking_app.ui.screens.Profile.ChangePasswordScreen
import com.example.movie_booking_app.ui.screens.Profile.PersonalInfoScreen
import com.example.movie_booking_app.ui.screens.Profile.ProfileScreen

/**
 * Định nghĩa các màn hình trong ứng dụng
 */
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

    fun createRoute(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                route.replace("{$arg}", arg)
            }
        }
    }
}

/**
 * Quản lý điều hướng trong ứng dụng và tích hợp Firebase Authentication
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val movieViewModel: MovieViewModel = viewModel()
    val newsViewModel: NewsViewModel = viewModel() // Khởi tạo NewsViewModel
    val authViewModel: AuthViewModel = viewModel()

    // Theo dõi trạng thái đăng nhập để điều hướng phù hợp
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.MovieList.route
    ) {
        // 1. ROUTE MÀN HÌNH CHÍNH
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
                }
            )
        }

        // 2. ROUTE CHI TIẾT PHIM
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
                        // Kiểm tra đã đăng nhập chưa trước khi đặt vé
                        if (authState is AuthState.Authenticated) {
                            // Điều hướng đến màn hình đặt vé
                            // navController.navigate("booking/$movieId")
                        } else {
                            // Cần đăng nhập trước
                            navController.navigate(Screen.Login.route)
                        }
                    },
                    movieViewModel = movieViewModel,
                    newsViewModel = newsViewModel,
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

        // 3. ROUTE CHI TIẾT TIN TỨC
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

        // 4. ROUTE TẤT CẢ TIN TỨC
        composable(Screen.AllNews.route) {
            AllNewsScreen(
                viewModel = newsViewModel, // Thay đổi: Sử dụng NewsViewModel
                onBackClick = { navController.popBackStack() },
                onNewsClick = { news ->
                    newsViewModel.selectNews(news) // Sử dụng NewsViewModel
                    navController.navigate(Screen.NewsDetail.route)
                }
            )
        }

        // 5. ROUTE TẤT CẢ VIDEO
        composable(Screen.AllVideos.route) {
            AllVideosScreen(
                viewModel = movieViewModel,
                onBackClick = { navController.popBackStack() },
                onVideoClick = { movie -> },
                initialVideoId = null
            )
        }

        // 6. ROUTE TẤT CẢ VIDEO VỚI PARAM
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

        // 7. ROUTE ĐĂNG NHẬP
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

        // 8. ROUTE ĐĂNG KÝ
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // 9. ROUTE PROFILE - Màn hình hồ sơ người dùng
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onPersonalInfoClick = { navController.navigate("personal_info") },
                onChangePasswordClick = { navController.navigate("change_password") },
                onTransactionHistoryClick = { navController.navigate("transaction_history") },
                onLogoutClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // Thêm route cho màn hình thông tin cá nhân
        composable("personal_info") {
            PersonalInfoScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Trong navigation system của bạn
        composable("change_password") {
            ChangePasswordScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}