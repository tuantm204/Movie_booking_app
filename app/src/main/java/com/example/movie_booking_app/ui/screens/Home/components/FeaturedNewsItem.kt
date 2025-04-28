package com.example.movie_booking_app.ui.screens.Home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movie_booking_app.data.model.News

@Composable
fun FeaturedNewsItem(
    news: News,
    onNewsClick: (News) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
                    .clickable { onNewsClick(news) }
            ) {
                Box {
                    AsyncImage(
                        model = news.image,
                        contentDescription = news.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}