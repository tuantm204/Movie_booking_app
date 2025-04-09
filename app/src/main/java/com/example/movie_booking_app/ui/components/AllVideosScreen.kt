package com.example.movie_booking_app.ui.components

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.Movie
import com.example.movie_booking_app.data.repository.MovieViewModel
import com.example.movie_booking_app.utils.getAutoPlayUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllVideosScreen(
    viewModel: MovieViewModel,
    onBackClick: () -> Unit,
    onVideoClick: (Movie) -> Unit,
    initialVideoId: String? = null // Thêm tham số mới để biết video nào cần phát ngay lập tức
) {
    // Lấy danh sách phim có trailer
    val nowPlayingMovies by viewModel.nowPlayingMovies.collectAsState()
    val upcomingMovies by viewModel.upcomingMovies.collectAsState()

    // Kết hợp và lọc phim có trailer
    val allMovies = (nowPlayingMovies + upcomingMovies).distinctBy { it.title }
    val moviesWithTrailer = allMovies.filter { !it.trailer.isNullOrEmpty() }

    // Theo dõi ID của video đang phát (nếu có) - khởi tạo với giá trị initialVideoId
    var currentPlayingVideoId by remember { mutableStateOf(initialVideoId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tất cả trailer phim") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (moviesWithTrailer.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có trailer nào",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(moviesWithTrailer) { movie ->
                    val movieId = movie.title ?: ""
                    val isPlaying = currentPlayingVideoId == movieId

                    InlineVideoItem(
                        movie = movie,
                        isPlaying = isPlaying,
                        onPlayClick = {
                            // Nếu nhấn vào video đang phát, dừng nó
                            if (isPlaying) {
                                currentPlayingVideoId = null
                            } else {
                                // Nếu không, phát video mới
                                currentPlayingVideoId = movieId
                            }
                        },
                        onCloseClick = {
                            // Khi nhấn đóng, dừng video
                            if (currentPlayingVideoId == movieId) {
                                currentPlayingVideoId = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InlineVideoItem(
    movie: Movie,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isPlaying) {
                // Hiển thị WebView khi đang phát video
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // WebView để phát video
                    AndroidView(
                        factory = {
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webChromeClient = WebChromeClient()
                                webViewClient = WebViewClient()
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                // Tải URL trailer
                                if (!movie.trailer.isNullOrEmpty()) {
                                    loadUrl(getAutoPlayUrl(movie.trailer))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Nút đóng video
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .zIndex(10f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Đóng video",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                // Hiển thị thumbnail khi không phát video
                AsyncImage(
                    model = movie.imagelink,
                    contentDescription = "${movie.title} - Trailer",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Nút play ở chính giữa
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .background(color = Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                        .clickable(onClick = onPlayClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Xem trailer",
                        modifier = Modifier.size(30.dp),
                        tint = Color.White
                    )
                }

                // Thông tin phim ở dưới cùng
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = movie.title ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.7f),
                                blurRadius = 6f
                            )
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Khởi chiếu: ${movie.releaseDate ?: "Đang cập nhật"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.7f),
                                blurRadius = 6f
                            )
                        ),
                        color = Color.White,
                    )
                }
            }
        }
    }
}