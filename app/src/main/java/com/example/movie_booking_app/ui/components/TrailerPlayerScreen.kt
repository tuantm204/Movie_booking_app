package com.example.movie_booking_app.ui.components

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.example.movie_booking_app.utils.getAutoPlayUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailerPlayerScreen(
    trailerUrl: String,
    movieTitle: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = movieTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Hiển thị WebView cho trailer
            val webView = remember {
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false // Tự động phát
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }

            DisposableEffect(trailerUrl) {
                // Chuyển đổi URL thành format embed phù hợp (nếu là YouTube)
                val embedUrl = getAutoPlayUrl(trailerUrl)
                webView.loadUrl(embedUrl)

                onDispose {
                    // Dừng video khi rời khỏi màn hình
                    webView.loadUrl("about:blank")
                    webView.destroy()
                }
            }

            AndroidView(
                factory = { webView },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f) // Tỉ lệ video chuẩn
            )
        }
    }
}