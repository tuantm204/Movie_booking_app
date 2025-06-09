package com.example.movie_booking_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.example.movie_booking_app.data.model.Review
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun AllReviewScreen(
    reviews: List<Review>,
    filteredReviews: List<Review>,
    averageRating: Float,
    ratingsDistribution: Map<Int, Int>,
    currentFilter: Int,
    isLoading: Boolean,
    onFilterChanged: (Int) -> Unit,
    onHelpfulClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Tiêu đề
        Text(
            text = "Đánh giá & Nhận xét",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tổng quan đánh giá
        RatingOverview(
            averageRating = averageRating,
            totalReviews = reviews.size,
            ratingsDistribution = ratingsDistribution,
            currentFilter = currentFilter,
            onFilterChanged = onFilterChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Danh sách đánh giá đã lọc
        ReviewsList(
            reviews = filteredReviews,
            isLoading = isLoading,
            onHelpfulClick = onHelpfulClick
        )
    }
}

// Tổng quan đánh giá
@Composable
fun RatingOverview(
    averageRating: Float,
    totalReviews: Int,
    ratingsDistribution: Map<Int, Int>,
    currentFilter: Int,
    onFilterChanged: (Int) -> Unit
) {
    val primaryRed = Color(0xFFE71A0F)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Đánh giá từ người xem",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị điểm trung bình
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format("%.1f", averageRating),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = primaryRed
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    // Hiển thị các ngôi sao
                    Row {
                        val filledStars = averageRating.roundToInt()
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= filledStars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (i <= filledStars) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "$totalReviews đánh giá",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị phân bố đánh giá
            for (i in 5 downTo 1) {
                val count = ratingsDistribution[i] ?: 0
                val percentage = if (totalReviews > 0) (count.toFloat() / totalReviews) * 100 else 0f

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "$i",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        modifier = Modifier.width(16.dp)
                    )

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(if (percentage > 0) percentage / 100 else 0f)
                                .background(primaryRed, RoundedCornerShape(4.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = count.toString(),
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Lọc đánh giá",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp), // Giảm khoảng cách giữa các chip
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = currentFilter == 0,
                    onClick = { onFilterChanged(0) },
                    label = {
                        Text(
                            "Tất cả",
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.height(28.dp), // Giảm chiều cao
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryRed,
                        selectedLabelColor = Color.White
                    )
                )

                for (rating in 5 downTo 1) {
                    FilterChip(
                        selected = currentFilter == rating,
                        onClick = { onFilterChanged(rating) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "$rating",
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp) // Giảm kích thước icon
                                )
                            }
                        },
                        modifier = Modifier.height(28.dp), // Giảm chiều cao
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

// Danh sách đánh giá đã lọc
@Composable
fun ReviewsList(
    reviews: List<Review>,
    isLoading: Boolean,
    onHelpfulClick: (String) -> Unit
) {
    val primaryRed = Color(0xFFE71A0F)
    // State để theo dõi xem có hiển thị tất cả đánh giá hay không
    var showAllReviews by remember { mutableStateOf(false) }

    // Danh sách đánh giá được hiển thị (giới hạn 5 hoặc tất cả)
    val displayedReviews = if (showAllReviews || reviews.size <= 5) {
        reviews
    } else {
        reviews.take(5)
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryRed)
        }
        return
    }

    if (reviews.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có đánh giá nào cho bộ lọc này",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Nhận xét từ người xem",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        // Hiển thị chỉ 5 đánh giá hoặc tất cả tùy thuộc vào state
        displayedReviews.forEach { review ->
            ReviewItem(review = review, onHelpfulClick = onHelpfulClick)
        }

        // Hiển thị nút "Xem thêm" nếu có nhiều hơn 5 đánh giá và chưa hiển thị tất cả
        if (reviews.size > 5 && !showAllReviews) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showAllReviews = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xem thêm ${reviews.size - 5} đánh giá",
                    color = primaryRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Xem thêm",
                    tint = primaryRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        // Nếu đang hiển thị tất cả, hiển thị nút "Thu gọn"
        else if (reviews.size > 5 && showAllReviews) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showAllReviews = false },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thu gọn",
                    color = primaryRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Thu gọn",
                    tint = primaryRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

//Đánh giá
@Composable
fun ReviewItem(review: Review, onHelpfulClick: (String) -> Unit) {
    // Theo dõi trạng thái mở rộng của nhận xét
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }, // Click vào card để mở rộng nội dung
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header - User và Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User icon và ngày đánh giá
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Người dùng",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale("vi")).format(review.reviewDate),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Rating stars
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= review.rating) Color(0xFFFFD700) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment - Giới hạn hiển thị 3 dòng nếu chưa mở rộng
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
                )

                // Chỉ hiển thị nút "Xem thêm" nếu comment dài
                if (!isExpanded && review.comment.lines().size > 3) {
                    Text(
                        text = "Xem thêm",
                        color = Color(0xFFE71A0F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { isExpanded = true }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}