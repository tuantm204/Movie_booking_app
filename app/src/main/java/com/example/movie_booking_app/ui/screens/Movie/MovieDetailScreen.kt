//package com.example.movie_booking_app.ui.screens.Movie
//
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.CalendarToday
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.foundation.clickable
//import android.view.ViewGroup
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import androidx.compose.foundation.background
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.AccessTime
//import androidx.compose.ui.unit.sp
//import com.example.movie_booking_app.data.model.Movie
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MovieDetailScreen(
//    movie: Movie,
//    onBackClick: () -> Unit,
//    onBookClick: () -> Unit,
//    onPlayTrailerClick: (String) -> Unit = {}
//) {
//    val context = LocalContext.current
//    // Thêm biến trạng thái để theo dõi việc hiển thị trailer
//    var isTrailerPlaying by remember { mutableStateOf(false) }
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(text = "Phim") },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
//                    }
//                }
//            )
//        },
//        // Thêm bottomBar để chứa nút đặt vé cố định
//        bottomBar = {
//            Surface(
//                tonalElevation = 8.dp,
//                shadowElevation = 8.dp
//            ) {
//                Button(
//                    onClick = onBookClick,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 12.dp)
//                        .height(56.dp)
//                ) {
//                    Text("Đặt Vé Ngay")
//                }
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .verticalScroll(rememberScrollState())
//        ) {
//            // Phần header với ảnh và tiêu đề
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(360.dp) // Chiều cao tổng của box chứa trailer
//            ) {
//                // Trailer background
//                AsyncImage(
//                    model = movie.imagelink,
//                    contentDescription = movie.title,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(250.dp) // Chiều cao của phần nền trailer (nửa trên)
//                        .align(Alignment.TopCenter)
//                        .clickable { // Thêm clickable để bấm vào ảnh cũng phát trailer
//                            try {
//                                val trailerUrl = movie.trailer
//
//                                if (trailerUrl.isNullOrEmpty()) {
//                                    Toast.makeText(context, "Không có trailer cho phim này", Toast.LENGTH_SHORT).show()
//                                    return@clickable
//                                }
//
//                                isTrailerPlaying = true
//                            } catch (e: Exception) {
//                                Log.e("TrailerDebug", "Lỗi khi xử lý trailer: ${e.message}", e)
//                                Toast.makeText(context, "Không thể mở trailer: ${e.message}", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                )
//
//                // Nút xem trailer - đặt ở giữa phần trailer
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.TopCenter)
//                        .padding(top = 110.dp) // Căn giữa phần trailer
//                        .size(40.dp)
//                        .border(
//                            width = 3.dp,
//                            color = androidx.compose.ui.graphics.Color.White,
//                            shape = androidx.compose.foundation.shape.CircleShape
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        Icons.Default.PlayArrow,
//                        contentDescription = "Xem trailer",
//                        modifier = Modifier.size(32.dp),
//                        tint = androidx.compose.ui.graphics.Color.White
//                    )
//                }
//
//                // Phần nền trắng phía dưới
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(70.dp)
//                        .align(Alignment.BottomCenter)
//                        .background(MaterialTheme.colorScheme.surface)
//                )
//
//                // Layout chứa poster và tiêu đề
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                        .align(Alignment.BottomCenter),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Poster phim - nửa trên nửa dưới
//                    Card(
//                        shape = RoundedCornerShape(8.dp),
//                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//                        modifier = Modifier
//                            .width(100.dp)
//                            .height(150.dp)
//                            .offset(y = (-35).dp) // Di chuyển lên trên để nửa nằm trên nền trailer
//                    ) {
//                        AsyncImage(
//                            model = movie.imagelink,
//                            contentDescription = "${movie.title} poster",
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier.fillMaxSize()
//                        )
//                    }
//
//                    // Tiêu đề phim bên cạnh poster
//                    Column(
//                        modifier = Modifier
//                            .padding(start = 16.dp, top = 0.dp)
//                            .weight(1f)
//                    ) {
//                        // Tiêu đề phim
//                        Text(
//                            text = movie.title ?: "Không có tên",
//                            fontSize = 14.sp, // Đặt kích thước chữ theo ý muốn
//                            fontWeight = FontWeight.Bold,
//                            color = androidx.compose.ui.graphics.Color.White,
//                            modifier = Modifier.offset(y = (-35).dp)
//                        )
//                        // Thông tin cơ bản
//                        Row {
//                            InfoChip(icon = Icons.Default.CalendarToday, text = movie.releaseDate ?: "N/A")
//                            Spacer(modifier = Modifier.width(8.dp))
//                            InfoChip(icon = Icons.Default.AccessTime, text = movie.duration ?: "N/A")
//                        }
//                    }
//                }
//            }
//
//            // Phần thông tin chi tiết
//            Column(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp)
//                    .offset(y = (-20).dp) // Thêm offset âm để đẩy toàn bộ phần nội dung lên trên
//            ) { // Giảm padding top từ 16.dp xuống 4.dp
//                // Phần nội dung phim
//                var expandedContent by remember { mutableStateOf(false) }
//
//                Column(modifier = Modifier.fillMaxWidth()) {
//                    Text(
//                        text = "Nội dung",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Spacer(modifier = Modifier.height(2.dp)) // Giảm từ 4.dp xuống 2.dp
//
//                    val movieDetails = movie.details ?: "Chưa có thông tin chi tiết về phim này."
//
//                    // Kiểm tra nội dung có dài không
//                    val needsReadMore = movieDetails.length > 200
//
//                    // Phần code hiển thị nội dung phim - giữ nguyên phần này từ code gốc
//                    if (needsReadMore) {
//                        // ... code hiển thị chi tiết với nút xem thêm - giữ nguyên
//                        val displayedText = if (expandedContent) movieDetails else {
//                            if (movieDetails.length > 200) {
//                                movieDetails.take(200) + "... "
//                            } else {
//                                movieDetails
//                            }
//                        }
//
//                        Box {
//                            Text(
//                                text = displayedText,
//                                style = MaterialTheme.typography.bodyMedium,
//                            )
//
//                            if (!expandedContent) {
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.End
//                                ) {
//                                    Text(
//                                        text = "Xem thêm",
//                                        color = MaterialTheme.colorScheme.primary,
//                                        fontWeight = FontWeight.Medium,
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        modifier = Modifier
//                                            .padding(top = 60.dp)
//                                            .clickable { expandedContent = true }
//                                    )
//                                }
//                            }
//                        }
//                    } else {
//                        Text(
//                            text = movieDetails,
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                    }
//                }
//
//                // Các thông tin khác - giữ nguyên
//                CompactInfoSection("Thể loại:", movie.genre)
//                CompactInfoSection("Đạo diễn:", movie.director)
//                CompactInfoSection("Diễn viên:", movie.actors)
//                CompactInfoSection("Ngôn ngữ:", movie.language)
//                CompactInfoSection("Kiểm duyệt:", movie.rated)
//
//                Spacer(modifier = Modifier.height(40.dp))
//                // Hiển thị dialog với WebView khi isTrailerPlaying là true và trailer URL không trống
//                if (isTrailerPlaying && !movie.trailer.isNullOrEmpty()) {
//                    Dialog(
//                        onDismissRequest = { isTrailerPlaying = false },
//                        properties = DialogProperties(
//                            dismissOnBackPress = true,
//                            dismissOnClickOutside = false,
//                            usePlatformDefaultWidth = false
//                        )
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(androidx.compose.ui.graphics.Color.Black)
//                        ) {
//                            // WebView để phát video
//                            AndroidView(
//                                factory = { context ->
//                                    WebView(context).apply {
//                                        layoutParams = ViewGroup.LayoutParams(
//                                            ViewGroup.LayoutParams.MATCH_PARENT,
//                                            ViewGroup.LayoutParams.MATCH_PARENT
//                                        )
//                                        webViewClient = WebViewClient()
//                                        settings.javaScriptEnabled = true
//                                        settings.mediaPlaybackRequiresUserGesture = false
//                                        settings.domStorageEnabled = true // Thêm dòng này
//                                        settings.loadsImagesAutomatically = true // Thêm dòng này
//
//                                        // Sử dụng URL đã được xử lý để tự động phát
//                                        loadUrl(getAutoPlayUrl(movie.trailer!!))
//                                    }
//                                },
//                                modifier = Modifier.fillMaxSize()
//                            )
//
//                            // Nút đóng video
//                            IconButton(
//                                onClick = { isTrailerPlaying = false },
//                                modifier = Modifier
//                                    .align(Alignment.TopEnd)
//                                    .padding(16.dp)
//                                    .size(35.dp)
//                                    .background(
//                                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f),
//                                        shape = androidx.compose.foundation.shape.CircleShape
//                                    )
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Close,
//                                    contentDescription = "Đóng",
//                                    tint = androidx.compose.ui.graphics.Color.White
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
//    Surface(
//        shape = RoundedCornerShape(12.dp),
//        color = MaterialTheme.colorScheme.surfaceVariant,
//    ) {
//        Row(
//            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                modifier = Modifier.size(14.dp)
//            )
//            Spacer(modifier = Modifier.width(2.dp))
//            Text(text = text, style = MaterialTheme.typography.bodySmall)
//        }
//    }
//}
//
//@Composable
//fun InfoSection(title: String, content: String?) {
//    Column(modifier = Modifier.padding(vertical = 4.dp)) {
//        Text(
//            text = title,
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold
//        )
//        Text(
//            text = content ?: "Không có thông tin",
//            style = MaterialTheme.typography.bodyMedium
//        )
//    }
//}
//// Thêm Composable cho hiển thị thông tin chi tiết trên một dòng
//@Composable
//fun CompactInfoSection(title: String, content: String?) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 0.dp),
//        verticalAlignment = Alignment.Top
//    ) {
//        Text(
//            text = title,
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.width(100.dp)
//        )
//        Text(
//            text = content ?: "Không có thông tin",
//            style = MaterialTheme.typography.bodyMedium,
//            modifier = Modifier.weight(1f)
//        )
//    }
//}
//private fun getAutoPlayUrl(url: String): String {
//    return when {
//        url.contains("youtube.com") -> {
//            if (url.contains("?")) {
//                "$url&autoplay=1&mute=0"
//            } else {
//                "$url?autoplay=1&mute=0"
//            }
//        }
//        url.contains("youtu.be") -> {
//            val videoId = url.substringAfterLast("/")
//            "https://www.youtube.com/embed/$videoId?autoplay=1&mute=0"
//        }
//        else -> url
//    }
//}

package com.example.movie_booking_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.ui.components.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.example.movie_booking_app.data.model.News
import com.example.movie_booking_app.data.repository.MovieViewModel
import com.example.movie_booking_app.ui.screens.Home.components.NewsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit,
    viewModel: MovieViewModel, // Thêm viewModel
    onNewsClick: (News) -> Unit, // Thêm xử lý click tin tức
    onViewAllNewsClick: () -> Unit // Thêm callback mới để xử lý nút "Tất cả"
) {
    var isTrailerPlaying by remember { mutableStateOf(false) }
    val newsList by viewModel.news.collectAsState() // Lấy danh sách tin tức

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Phim") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = Color.White, // Đặt màu nền là màu trắng
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(bottom = 12.dp) // Cách đáy 12.dp
            ) {
                Button(
                    onClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red // Đặt màu nền là màu đỏ
                    )
                ) {
                    Text("ĐẶT VÉ")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Phần header với ảnh, poster và tiêu đề
            MovieHeader(
                movie = movie,
                onTrailerClick = { isTrailerPlaying = true }
            )

            // Phần thông tin chi tiết
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp)
            ) {
                // Phần nội dung phim
                MovieContent(details = movie.details)
                Spacer(modifier = Modifier.height(16.dp))
                // Các thông tin khác
                MovieInfoDetails(movie = movie)
            }
            // Thêm News Section (đặt bên ngoài Column chính để chiếm full width)
            NewsSection(
                newsList = newsList,
                onNewsClick = onNewsClick,
                onViewAllClick = onViewAllNewsClick // Sử dụng callback mới
            )
            // Hiển thị dialog trailer khi cần
            if (isTrailerPlaying && !movie.trailer.isNullOrEmpty()) {
                MovieTrailerDialog(
                    trailerUrl = movie.trailer!!,
                    onDismiss = { isTrailerPlaying = false }
                )
            }
        }
    }
}