package com.example.movie_booking_app.ui.screens.Auth

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movie_booking_app.R
import com.example.movie_booking_app.viewmodel.AuthViewModel
import java.util.Calendar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import com.example.movie_booking_app.data.repository.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    // State quản lý dữ liệu form
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // State cho các dropdown
    var birthDay by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var favoriteCinema by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }

    // State cho các dropdown mở/đóng
    var isGenderExpanded by remember { mutableStateOf(false) }
    var isRegionExpanded by remember { mutableStateOf(false) }
    var isDistrictExpanded by remember { mutableStateOf(false) }
    var isCinemaExpanded by remember { mutableStateOf(false) }

    // State cho các checkbox
    var checkPrivacy by remember { mutableStateOf(false) }
    var checkAccuracy by remember { mutableStateOf(false) }
    var checkResponsibility by remember { mutableStateOf(false) }
    var checkTerms by remember { mutableStateOf(false) }

    // State quản lý trạng thái UI
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val primaryRed = Color(0xFFE71A0F)
    val darkGray = Color(0xFF333333)

    val genderOptions = listOf("Nam", "Nữ", "Khác")

    // DANH SÁCH ĐẦY ĐỦ TỈNH THÀNH PHỐ VIỆT NAM VÀ QUẬN HUYỆN
    val cityDistrictMap = remember {
        mapOf(
            "Hà Nội" to listOf(
                "Ba Đình", "Hoàn Kiếm", "Tây Hồ", "Long Biên", "Cầu Giấy",
                "Đống Đa", "Hai Bà Trưng", "Hoàng Mai", "Thanh Xuân", "Bắc Từ Liêm",
                "Nam Từ Liêm", "Hà Đông", "Sơn Tây", "Ba Vì", "Chương Mỹ",
                "Đan Phượng", "Đông Anh", "Gia Lâm", "Hoài Đức", "Mê Linh",
                "Mỹ Đức", "Phú Xuyên", "Phúc Thọ", "Quốc Oai", "Sóc Sơn",
                "Thạch Thất", "Thanh Oai", "Thanh Trì", "Thường Tín", "Ứng Hòa"
            ),
            "TP. Hồ Chí Minh" to listOf(
                "Quận 1", "Quận 3", "Quận 4", "Quận 5", "Quận 6",
                "Quận 7", "Quận 8", "Quận 10", "Quận 11", "Quận 12",
                "Bình Tân", "Bình Thạnh", "Gò Vấp", "Phú Nhuận", "Tân Bình",
                "Tân Phú", "Thủ Đức", "Bình Chánh", "Cần Giờ", "Củ Chi",
                "Hóc Môn", "Nhà Bè"
            ),
            "Đà Nẵng" to listOf(
                "Hải Châu", "Thanh Khê", "Sơn Trà", "Ngũ Hành Sơn", "Liên Chiểu",
                "Cẩm Lệ", "Hòa Vang", "Hoàng Sa"
            ),
            "Hải Phòng" to listOf(
                "Hồng Bàng", "Ngô Quyền", "Lê Chân", "Hải An", "Kiến An",
                "Đồ Sơn", "Dương Kinh", "An Dương", "An Lão", "Bạch Long Vĩ",
                "Cát Hải", "Kiến Thụy", "Thủy Nguyên", "Tiên Lãng", "Vĩnh Bảo"
            ),
            "Cần Thơ" to listOf(
                "Ninh Kiều", "Bình Thủy", "Cái Răng", "Ô Môn", "Thốt Nốt",
                "Cờ Đỏ", "Phong Điền", "Thới Lai", "Vĩnh Thạnh"
            ),
            "An Giang" to listOf(
                "Long Xuyên", "Châu Đốc", "An Phú", "Châu Phú", "Châu Thành",
                "Chợ Mới", "Phú Tân", "Tân Châu", "Thoại Sơn", "Tịnh Biên", "Tri Tôn"
            ),
            "Bà Rịa - Vũng Tàu" to listOf(
                "Vũng Tàu", "Bà Rịa", "Châu Đức", "Côn Đảo", "Đất Đỏ",
                "Long Điền", "Tân Thành", "Xuyên Mộc"
            ),
            "Bắc Giang" to listOf(
                "Bắc Giang", "Hiệp Hòa", "Lạng Giang", "Lục Nam", "Lục Ngạn",
                "Sơn Động", "Tân Yên", "Việt Yên", "Yên Dũng", "Yên Thế"
            ),
            "Bắc Kạn" to listOf(
                "Bắc Kạn", "Ba Bể", "Bạch Thông", "Chợ Đồn", "Chợ Mới",
                "Na Rì", "Ngân Sơn", "Pác Nặm"
            ),
            "Bạc Liêu" to listOf(
                "Bạc Liêu", "Đông Hải", "Giá Rai", "Hòa Bình", "Hồng Dân",
                "Phước Long", "Vĩnh Lợi"
            ),
            "Bắc Ninh" to listOf(
                "Bắc Ninh", "Gia Bình", "Lương Tài", "Quế Võ", "Thuận Thành",
                "Tiên Du", "Từ Sơn", "Yên Phong"
            ),
            "Bến Tre" to listOf(
                "Bến Tre", "Ba Tri", "Bình Đại", "Châu Thành", "Chợ Lách",
                "Giồng Trôm", "Mỏ Cày Bắc", "Mỏ Cày Nam", "Thạnh Phú"
            ),
            "Bình Định" to listOf(
                "Quy Nhơn", "An Lão", "An Nhơn", "Hoài Ân", "Hoài Nhơn",
                "Phù Cát", "Phù Mỹ", "Tây Sơn", "Tuy Phước", "Vân Canh", "Vĩnh Thạnh"
            ),
            "Bình Dương" to listOf(
                "Thủ Dầu Một", "Bắc Tân Uyên", "Bàu Bàng", "Bến Cát", "Dầu Tiếng",
                "Dĩ An", "Phú Giáo", "Tân Uyên", "Thuận An"
            ),
            "Bình Phước" to listOf(
                "Đồng Xoài", "Bình Long", "Phước Long", "Bù Đăng", "Bù Đốp",
                "Bù Gia Mập", "Chơn Thành", "Đồng Phú", "Hớn Quản", "Lộc Ninh", "Phú Riềng"
            ),
            "Bình Thuận" to listOf(
                "Phan Thiết", "Bắc Bình", "Đức Linh", "Hàm Tân", "Hàm Thuận Bắc",
                "Hàm Thuận Nam", "La Gi", "Phú Quý", "Tánh Linh", "Tuy Phong"
            ),
            "Cà Mau" to listOf(
                "Cà Mau", "Cái Nước", "Đầm Dơi", "Năm Căn", "Ngọc Hiển",
                "Phú Tân", "Thới Bình", "Trần Văn Thời", "U Minh"
            ),
            "Cao Bằng" to listOf(
                "Cao Bằng", "Bảo Lạc", "Bảo Lâm", "Hạ Lang", "Hà Quảng",
                "Hòa An", "Nguyên Bình", "Quảng Hòa", "Thạch An", "Trùng Khánh"
            ),
            "Đắk Lắk" to listOf(
                "Buôn Ma Thuột", "Buôn Đôn", "Buôn Hồ", "Cư Kuin", "Cư M'gar",
                "Ea H'leo", "Ea Kar", "Ea Súp", "Krông Ana", "Krông Bông",
                "Krông Búk", "Krông Năng", "Krông Pắc", "Lắk", "M'Đrắk"
            ),
            "Đắk Nông" to listOf(
                "Gia Nghĩa", "Cư Jút", "Đắk Glong", "Đắk Mil", "Đắk R'lấp",
                "Đắk Song", "Krông Nô", "Tuy Đức"
            ),
            "Điện Biên" to listOf(
                "Điện Biên Phủ", "Điện Biên", "Điện Biên Đông", "Mường Ảng",
                "Mường Chà", "Mường Lay", "Mường Nhé", "Nậm Pồ", "Tủa Chùa", "Tuần Giáo"
            ),
            "Đồng Nai" to listOf(
                "Biên Hòa", "Cẩm Mỹ", "Định Quán", "Long Khánh", "Long Thành",
                "Nhơn Trạch", "Tân Phú", "Thống Nhất", "Trảng Bom", "Vĩnh Cửu", "Xuân Lộc"
            ),
            "Đồng Tháp" to listOf(
                "Cao Lãnh", "Châu Thành", "Hồng Ngự", "Lai Vung", "Lấp Vò",
                "Sa Đéc", "Tân Hồng", "Tháp Mười", "Thanh Bình", "Tam Nông"
            ),
            "Gia Lai" to listOf(
                "Pleiku", "An Khê", "Ayun Pa", "Chư Păh", "Chư Prông",
                "Chư Pưh", "Chư Sê", "Đak Đoa", "Đak Pơ", "Đức Cơ",
                "Ia Grai", "Ia Pa", "K'Bang", "Kông Chro", "Krông Pa",
                "Mang Yang", "Phú Thiện"
            ),
            "Hà Giang" to listOf(
                "Hà Giang", "Bắc Mê", "Bắc Quang", "Đồng Văn", "Hoàng Su Phì",
                "Mèo Vạc", "Quản Bạ", "Quang Bình", "Vị Xuyên", "Xín Mần", "Yên Minh"
            ),
            "Hà Nam" to listOf(
                "Phủ Lý", "Bình Lục", "Duy Tiên", "Kim Bảng", "Lý Nhân",
                "Thanh Liêm"
            ),
            "Hà Tĩnh" to listOf(
                "Hà Tĩnh", "Can Lộc", "Cẩm Xuyên", "Đức Thọ", "Hồng Lĩnh",
                "Hương Khê", "Hương Sơn", "Kỳ Anh", "Lộc Hà", "Nghi Xuân",
                "Thạch Hà", "Vũ Quang"
            ),
            "Hải Dương" to listOf(
                "Hải Dương", "Bình Giang", "Cẩm Giàng", "Chí Linh", "Gia Lộc",
                "Kim Thành", "Kinh Môn", "Nam Sách", "Ninh Giang", "Thanh Hà",
                "Thanh Miện", "Tứ Kỳ"
            ),
            "Hậu Giang" to listOf(
                "Vị Thanh", "Châu Thành", "Châu Thành A", "Long Mỹ", "Ngã Bảy",
                "Phụng Hiệp", "Vị Thủy"
            ),
            "Hòa Bình" to listOf(
                "Hòa Bình", "Cao Phong", "Đà Bắc", "Kim Bôi", "Lạc Sơn",
                "Lạc Thủy", "Lương Sơn", "Mai Châu", "Tân Lạc", "Yên Thủy"
            ),
            "Hưng Yên" to listOf(
                "Hưng Yên", "Ân Thi", "Khoái Châu", "Kim Động", "Mỹ Hào",
                "Phù Cừ", "Tiên Lữ", "Văn Giang", "Văn Lâm", "Yên Mỹ"
            ),
            "Khánh Hòa" to listOf(
                "Nha Trang", "Cam Lâm", "Cam Ranh", "Diên Khánh", "Khánh Sơn",
                "Khánh Vĩnh", "Ninh Hòa", "Trường Sa", "Vạn Ninh"
            ),
            "Kiên Giang" to listOf(
                "Rạch Giá", "An Biên", "An Minh", "Châu Thành", "Giang Thành",
                "Giồng Riềng", "Gò Quao", "Hà Tiên", "Hòn Đất", "Kiên Hải",
                "Kiên Lương", "Phú Quốc", "Tân Hiệp", "U Minh Thượng", "Vĩnh Thuận"
            ),
            "Kon Tum" to listOf(
                "Kon Tum", "Đắk Glei", "Đắk Hà", "Đắk Tô", "Ia H'Drai",
                "Kon Plông", "Kon Rẫy", "Ngọc Hồi", "Sa Thầy", "Tu Mơ Rông"
            ),
            "Lai Châu" to listOf(
                "Lai Châu", "Mường Tè", "Nậm Nhùn", "Phong Thổ", "Sìn Hồ",
                "Tam Đường", "Tân Uyên", "Than Uyên"
            ),
            "Lâm Đồng" to listOf(
                "Đà Lạt", "Bảo Lâm", "Bảo Lộc", "Cát Tiên", "Đạ Huoai",
                "Đạ Tẻh", "Đam Rông", "Di Linh", "Đơn Dương", "Đức Trọng",
                "Lạc Dương", "Lâm Hà"
            ),
            "Lạng Sơn" to listOf(
                "Lạng Sơn", "Bắc Sơn", "Bình Gia", "Cao Lộc", "Chi Lăng",
                "Đình Lập", "Hữu Lũng", "Lộc Bình", "Tràng Định", "Văn Lãng", "Văn Quan"
            ),
            "Lào Cai" to listOf(
                "Lào Cai", "Bắc Hà", "Bảo Thắng", "Bảo Yên", "Bát Xát",
                "Mường Khương", "Sa Pa", "Si Ma Cai", "Văn Bàn"
            ),
            "Long An" to listOf(
                "Tân An", "Bến Lức", "Cần Đước", "Cần Giuộc", "Châu Thành",
                "Đức Hòa", "Đức Huệ", "Mộc Hóa", "Tân Hưng", "Tân Thạnh",
                "Tân Trụ", "Thạnh Hóa", "Thủ Thừa", "Vĩnh Hưng"
            ),
            "Nam Định" to listOf(
                "Nam Định", "Giao Thủy", "Hải Hậu", "Mỹ Lộc", "Nam Trực",
                "Nghĩa Hưng", "Trực Ninh", "Vụ Bản", "Xuân Trường", "Ý Yên"
            ),
            "Nghệ An" to listOf(
                "Vinh", "Anh Sơn", "Con Cuông", "Diễn Châu", "Đô Lương",
                "Hoàng Mai", "Hưng Nguyên", "Kỳ Sơn", "Nam Đàn", "Nghi Lộc",
                "Nghĩa Đàn", "Quế Phong", "Quỳ Châu", "Quỳ Hợp", "Quỳnh Lưu",
                "Tân Kỳ", "Thái Hòa", "Thanh Chương", "Tương Dương", "Yên Thành"
            ),
            "Ninh Bình" to listOf(
                "Ninh Bình", "Gia Viễn", "Hoa Lư", "Kim Sơn", "Nho Quan",
                "Tam Điệp", "Yên Khánh", "Yên Mô"
            ),
            "Ninh Thuận" to listOf(
                "Phan Rang - Tháp Chàm", "Bác Ái", "Ninh Hải", "Ninh Phước",
                "Ninh Sơn", "Thuận Bắc", "Thuận Nam"
            ),
            "Phú Thọ" to listOf(
                "Việt Trì", "Cẩm Khê", "Đoan Hùng", "Hạ Hòa", "Lâm Thao",
                "Phú Ninh", "Phù Ninh", "Tam Nông", "Tân Sơn", "Thanh Ba",
                "Thanh Sơn", "Thanh Thủy", "Yên Lập"
            ),
            "Phú Yên" to listOf(
                "Tuy Hòa", "Đông Hòa", "Đồng Xuân", "Phú Hòa", "Sơn Hòa",
                "Sông Cầu", "Sông Hinh", "Tây Hòa", "Tuy An"
            ),
            "Quảng Bình" to listOf(
                "Đồng Hới", "Ba Đồn", "Bố Trạch", "Lệ Thủy", "Minh Hóa",
                "Quảng Ninh", "Quảng Trạch", "Tuyên Hóa"
            ),
            "Quảng Nam" to listOf(
                "Tam Kỳ", "Bắc Trà My", "Duy Xuyên", "Đại Lộc", "Điện Bàn",
                "Đông Giang", "Hiệp Đức", "Hội An", "Nam Giang", "Nam Trà My",
                "Nông Sơn", "Núi Thành", "Phú Ninh", "Phước Sơn", "Quế Sơn",
                "Tây Giang", "Thăng Bình", "Tiên Phước"
            ),
            "Quảng Ngãi" to listOf(
                "Quảng Ngãi", "Ba Tơ", "Bình Sơn", "Đức Phổ", "Lý Sơn",
                "Minh Long", "Mộ Đức", "Nghĩa Hành", "Sơn Hà", "Sơn Tây",
                "Sơn Tịnh", "Tây Trà", "Trà Bồng", "Tư Nghĩa"
            ),
            "Quảng Ninh" to listOf(
                "Hạ Long", "Ba Chẽ", "Bình Liêu", "Cẩm Phả", "Cô Tô",
                "Đầm Hà", "Đông Triều", "Hải Hà", "Móng Cái", "Quảng Yên",
                "Tiên Yên", "Uông Bí", "Vân Đồn"
            ),
            "Quảng Trị" to listOf(
                "Đông Hà", "Cam Lộ", "Cồn Cỏ", "Đa Krông", "Gio Linh",
                "Hải Lăng", "Hướng Hóa", "Quảng Trị", "Triệu Phong", "Vĩnh Linh"
            ),
            "Sóc Trăng" to listOf(
                "Sóc Trăng", "Châu Thành", "Cù Lao Dung", "Kế Sách", "Long Phú",
                "Mỹ Tú", "Mỹ Xuyên", "Ngã Năm", "Thạnh Trị", "Trần Đề", "Vĩnh Châu"
            ),
            "Sơn La" to listOf(
                "Sơn La", "Bắc Yên", "Mai Sơn", "Mộc Châu", "Mường La",
                "Phù Yên", "Quỳnh Nhai", "Sông Mã", "Sốp Cộp", "Thuận Châu",
                "Vân Hồ", "Yên Châu"
            ),
            "Tây Ninh" to listOf(
                "Tây Ninh", "Bến Cầu", "Châu Thành", "Dương Minh Châu", "Gò Dầu",
                "Hòa Thành", "Tân Biên", "Tân Châu", "Trảng Bàng"
            ),
            "Thái Bình" to listOf(
                "Thái Bình", "Đông Hưng", "Hưng Hà", "Kiến Xương", "Quỳnh Phụ",
                "Thái Thụy", "Tiền Hải", "Vũ Thư"
            ),
            "Thái Nguyên" to listOf(
                "Thái Nguyên", "Đại Từ", "Định Hóa", "Đồng Hỷ", "Phổ Yên",
                "Phú Bình", "Phú Lương", "Sông Công", "Võ Nhai"
            ),
            "Thanh Hóa" to listOf(
                "Thanh Hóa", "Bá Thước", "Bỉm Sơn", "Cẩm Thủy", "Đông Sơn",
                "Hà Trung", "Hậu Lộc", "Hoằng Hóa", "Lang Chánh", "Mường Lát",
                "Nga Sơn", "Nghi Sơn", "Ngọc Lặc", "Như Thanh", "Như Xuân",
                "Nông Cống", "Quan Hóa", "Quan Sơn", "Quảng Xương", "Sầm Sơn",
                "Thạch Thành", "Thiệu Hóa", "Thọ Xuân", "Thường Xuân", "Triệu Sơn",
                "Vĩnh Lộc", "Yên Định"
            ),
            "Thừa Thiên Huế" to listOf(
                "Huế", "A Lưới", "Nam Đông", "Phong Điền", "Phú Lộc",
                "Phú Vang", "Quảng Điền"
            ),
            "Tiền Giang" to listOf(
                "Mỹ Tho", "Cái Bè", "Cai Lậy", "Châu Thành", "Chợ Gạo",
                "Gò Công", "Gò Công Đông", "Gò Công Tây", "Tân Phú Đông", "Tân Phước"
            ),
            "Trà Vinh" to listOf(
                "Trà Vinh", "Càng Long", "Cầu Kè", "Cầu Ngang", "Châu Thành",
                "Duyên Hải", "Tiểu Cần", "Trà Cú"
            ),
            "Tuyên Quang" to listOf(
                "Tuyên Quang", "Chiêm Hóa", "Hàm Yên", "Lâm Bình", "Na Hang",
                "Sơn Dương", "Yên Sơn"
            ),
            "Vĩnh Long" to listOf(
                "Vĩnh Long", "Bình Minh", "Bình Tân", "Long Hồ", "Mang Thít",
                "Tam Bình", "Trà Ôn", "Vũng Liêm"
            ),
            "Vĩnh Phúc" to listOf(
                "Vĩnh Yên", "Bình Xuyên", "Lập Thạch", "Phúc Yên", "Sông Lô",
                "Tam Dương", "Tam Đảo", "Vĩnh Tường", "Yên Lạc"
            ),
            "Yên Bái" to listOf(
                "Yên Bái", "Lục Yên", "Mù Cang Chải", "Nghĩa Lộ", "Trạm Tấu",
                "Trấn Yên", "Văn Chấn", "Văn Yên", "Yên Bình"
            )
        )
    }

    val regionOptions = cityDistrictMap.keys.toList().sorted()
    val districtOptions = cityDistrictMap[region]?.sorted() ?: emptyList()
    val cinemaOptions = listOf(
        "MBA Aeon Mall", "MBA Vincom Center", "MBA Crescent Mall", "MBA Pearl Plaza",
        "MBA Landmark 81", "MBA Royal City", "MBA Times City", "MBA Lotte Center",
        "MBA Saigon Centre", "MBA Estella Place", "MBA Sunrise City", "MBA Cantavil Premier"
    )

    // Theo dõi trạng thái đăng nhập từ AuthViewModel
    val authState by authViewModel.authState.collectAsState()

    // Xử lý LaunchedEffect
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                isLoading = false
                successMessage = "Đăng ký thành công!"
                onRegisterSuccess()
            }
            is AuthState.Error -> {
                isLoading = false
                errorMessage = (authState as AuthState.Error).message
            }
            AuthState.Loading -> {
                isLoading = true
            }
            AuthState.Unauthenticated -> {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đăng Ký", // Tiêu đề màn hình
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner01),
                contentDescription = "Banner MBA",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Thông tin cá nhân",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = darkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. TRƯỜNG HỌ TÊN
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ tên *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 2. TRƯỜNG SỐ ĐIỆN THOẠI
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 3. TRƯỜNG EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 4. TRƯỜNG MẬT KHẨU
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu *") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible)
                                    "Ẩn mật khẩu" else "Hiện mật khẩu"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 5. TRƯỜNG NGÀY SINH - SỬ DỤNG DATE PICKER
                val context = LocalContext.current
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                var datePickerDialog: DatePickerDialog? = remember { null }

                OutlinedTextField(
                    value = birthDay,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Ngày sinh *") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            datePickerDialog = DatePickerDialog(
                                context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    birthDay = String.format("%02d/%02d/%04d",
                                        selectedDay, selectedMonth + 1, selectedYear)
                                }, year, month, day
                            )

                            // Thiết lập giới hạn năm (từ năm 1950 đến nay)
                            val minCalendar = Calendar.getInstance()
                            minCalendar.set(1950, 0, 1) // 1/1/1950
                            datePickerDialog?.datePicker?.minDate = minCalendar.timeInMillis

                            // Không cho phép chọn ngày trong tương lai
                            datePickerDialog?.datePicker?.maxDate = System.currentTimeMillis()

                            // Hiển thị DatePicker
                            datePickerDialog?.show()
                        }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Chọn ngày sinh"
                            )
                        }
                    },
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        datePickerDialog = DatePickerDialog(
                                            context,
                                            { _, selectedYear, selectedMonth, selectedDay ->
                                                birthDay = String.format("%02d/%02d/%04d",
                                                    selectedDay, selectedMonth + 1, selectedYear)
                                            }, year, month, day
                                        )

                                        // Thiết lập giới hạn năm (từ năm 1950 đến nay)
                                        val minCalendar = Calendar.getInstance()
                                        minCalendar.set(1950, 0, 1) // 1/1/1950
                                        datePickerDialog?.datePicker?.minDate = minCalendar.timeInMillis
                                        datePickerDialog?.datePicker?.maxDate = System.currentTimeMillis()
                                        datePickerDialog?.show()
                                    }
                                }
                            }
                        }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 6. DROPDOWN GIỚI TÍNH
                ExposedDropdownMenuBox(
                    expanded = isGenderExpanded,
                    onExpandedChange = { isGenderExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Giới tính *") },
                        leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGenderExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryRed,
                            focusedLabelColor = primaryRed
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isGenderExpanded,
                        onDismissRequest = { isGenderExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    isGenderExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 7. DROPDOWN KHU VỰC - CẬP NHẬT DANH SÁCH TỈNH THÀNH PHỐ
                ExposedDropdownMenuBox(
                    expanded = isRegionExpanded,
                    onExpandedChange = { isRegionExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tỉnh/Thành phố *") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRegionExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryRed,
                            focusedLabelColor = primaryRed
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isRegionExpanded,
                        onDismissRequest = { isRegionExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        regionOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    region = option
                                    isRegionExpanded = false
                                    // Reset district khi đổi region
                                    district = ""
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 8. DROPDOWN QUẬN/HUYỆN - CẬP NHẬT DỰA TRÊN TỈNH/THÀNH PHỐ ĐÃ CHỌN
                ExposedDropdownMenuBox(
                    expanded = isDistrictExpanded,
                    onExpandedChange = {
                        // Chỉ cho phép mở dropdown khi đã chọn thành phố
                        if (region.isNotEmpty()) isDistrictExpanded = it
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Quận/Huyện *") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDistrictExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryRed,
                            focusedLabelColor = primaryRed
                        ),
                        enabled = region.isNotEmpty()
                    )

                    ExposedDropdownMenu(
                        expanded = isDistrictExpanded,
                        onDismissRequest = { isDistrictExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        districtOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    district = option
                                    isDistrictExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 9. DROPDOWN RẠP YÊU THÍCH
                ExposedDropdownMenuBox(
                    expanded = isCinemaExpanded,
                    onExpandedChange = { isCinemaExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = favoriteCinema,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rạp yêu thích *") },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCinemaExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryRed,
                            focusedLabelColor = primaryRed
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isCinemaExpanded,
                        onDismissRequest = { isCinemaExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        cinemaOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    favoriteCinema = option
                                    isCinemaExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 10. TRƯỜNG MÃ GIỚI THIỆU (KHÔNG BẮT BUỘC)
                OutlinedTextField(
                    value = referralCode,
                    onValueChange = { referralCode = it },
                    label = { Text("Mã giới thiệu (nếu có)") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryRed,
                        focusedLabelColor = primaryRed
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                successMessage?.let {
                    Text(
                        text = it,
                        color = Color.Green,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 11. PHẦN CHECKBOX XÁC NHẬN
                Text(
                    "Điều khoản và cam kết",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = darkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Checkbox 1 - Chính sách bảo mật
                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = checkPrivacy,
                        onCheckedChange = { checkPrivacy = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryRed
                        )
                    )
                    Text(
                        "Đồng ý MBA xử lý dữ liệu cá nhân theo chính sách bảo mật",
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                    )
                }

                // Checkbox 2 - Thông tin chính xác
                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = checkAccuracy,
                        onCheckedChange = { checkAccuracy = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryRed
                        )
                    )
                    Text(
                        "Cam kết thông tin cá nhân là chính xác, khớp với giấy tờ tùy thân",
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                    )
                }

                // Checkbox 3 - Trách nhiệm thông tin
                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = checkResponsibility,
                        onCheckedChange = { checkResponsibility = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryRed
                        )
                    )
                    Text(
                        "Nếu thông tin sai lệch, không được cập nhật quyền lợi thành viên",
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                    )
                }

                // Checkbox 4 - Điều khoản sử dụng
                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = checkTerms,
                        onCheckedChange = { checkTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryRed
                        )
                    )
                    Text(
                        "Đồng ý với Điều Khoản Sử Dụng của MBA Việt Nam",
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                // 12. NÚT ĐĂNG KÝ
                val formValid = !isLoading &&
                        checkPrivacy && checkAccuracy && checkResponsibility && checkTerms &&
                        fullName.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty() &&
                        password.isNotEmpty() && birthDay.isNotEmpty() && gender.isNotEmpty() &&
                        region.isNotEmpty() && district.isNotEmpty() && favoriteCinema.isNotEmpty()

                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null
                        if (!isValidEmail(email)) {
                            errorMessage = "Email không hợp lệ"
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                            return@Button
                        }
                        authViewModel.signUp(
                            email = email,
                            password = password,
                            fullName = fullName,
                            phone = phone,
                            birthDay = birthDay,
                            gender = gender,
                            region = region,
                            district = district,
                            favoriteCinema = favoriteCinema
                        ) { success, message ->
                            if (!success) {
                                errorMessage = message ?: "Đăng ký thất bại"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryRed
                    ),
                    shape = RoundedCornerShape(4.dp),
                    enabled = formValid
                ) {
                    // Hiển thị loading indicator hoặc text
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "ĐĂNG KÝ",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(emailPattern.toRegex())
}