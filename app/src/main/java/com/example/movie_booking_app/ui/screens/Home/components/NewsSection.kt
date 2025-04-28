package com.example.movie_booking_app.ui.screens.Home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.News
import androidx.compose.material3.Surface
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@Composable
fun NewsSection(
    newsList: List<News>,
    onNewsClick: (News) -> Unit,
    onViewAllClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 1.dp, bottom = 1.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tin tức & Khuyến mãi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier.clickable { onViewAllClick() }
                    ) {
                        Text(
                            text = "Tất cả",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.background(Color.White)
                ) {
                    items(newsList) { news ->
                        NewsItem(news = news, onClick = { onNewsClick(news) })
                    }
                }
            }
        }
    }
}

@Composable
fun NewsItem(
    news: News,
    onClick: () -> Unit
) {
    // Box ngoài cùng có viền đen, không bo góc
    Box(
        modifier = Modifier
            .width(178.dp)
            .height(220.dp)
            .clickable(onClick = onClick)
            .background(Color.White) // Nền trắng
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Phần ảnh tin tức - chỉ bo góc phần ảnh
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = news.image,
                    contentDescription = news.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)) // Chỉ bo góc cho ảnh
                )

                // Tag "Hot" nếu là tin được quảng bá
                if (news.isPromoted) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Red)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "Hot",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Phần nội dung tin tức - không bo góc, chữ trên nền trắng
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = news.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}