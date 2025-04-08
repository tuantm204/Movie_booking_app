package com.example.movie_booking_app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MovieContent(details: String?) {
    var expandedContent by remember { mutableStateOf(false) }
    val movieDetails = details ?: "Chưa có thông tin chi tiết về phim này."
    val needsReadMore = movieDetails.length > 200

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Nội dung",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (needsReadMore) {
            val displayedText = if (expandedContent) movieDetails else {
                if (movieDetails.length > 200) {
                    movieDetails.take(200) + "... "
                } else {
                    movieDetails
                }
            }

            Box {
                Text(
                    text = displayedText,
                    style = MaterialTheme.typography.bodyMedium,
                )

                if (!expandedContent) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Xem thêm",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(top = 60.dp)
                                .clickable { expandedContent = true }
                        )
                    }
                }
            }
        } else {
            Text(
                text = movieDetails,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}