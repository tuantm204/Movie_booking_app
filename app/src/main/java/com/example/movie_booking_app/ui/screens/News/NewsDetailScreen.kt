package com.example.movie_booking_app.ui.screens.News

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.News

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    news: News,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tin tức & Ưu đãi")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = news.bannerImage.ifEmpty { news.image },
                contentDescription = news.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))


                Text(
                    text = news.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                val backgroundColor = MaterialTheme.colorScheme.background
                val textColor = MaterialTheme.colorScheme.onBackground
                val backgroundColorHex = String.format("#%06X", 0xFFFFFF and backgroundColor.hashCode())
                val textColorHex = String.format("#%06X", 0xFFFFFF and textColor.hashCode())

                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.defaultFontSize = 16
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            val htmlData = """
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body {
                                            font-family: 'Roboto', sans-serif;
                                            line-height: 1.6;
                                            color: $textColorHex;
                                            background-color: $backgroundColorHex;
                                            padding: 0;
                                            margin: 0;
                                        }
                                        img {
                                            max-width: 100%;
                                            height: auto;
                                        }
                                        .news-content {
                                            padding: 0;
                                        }
                                    </style>
                                </head>
                                <body>
                                    ${news.content}
                                </body>
                                </html>
                            """.trimIndent()
                            loadDataWithBaseURL(
                                null,
                                htmlData,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp)
                )
            }
        }
    }
}