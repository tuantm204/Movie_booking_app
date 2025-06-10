# Movie Booking App

Ứng dụng đặt vé xem phim được phát triển bằng Kotlin và Jetpack Compose, sử dụng Firebase làm backend.

## 🎯 Tính năng chính

- **Đăng nhập & Đăng ký**
  - Đăng nhập bằng email/password
  - Đăng ký tài khoản mới
  - Quên mật khẩu
  - Đăng xuất

- **Trang chủ**
  - Xem danh sách phim đang chiếu
  - Xem phim nổi bật
  - Xem tin tức & khuyến mãi
  - Tìm kiếm phim

- **Chi tiết phim**
  - Thông tin phim
  - Trailer
  - Đánh giá & nhận xét
  - Lịch chiếu
  - Đặt vé

- **Đặt vé**
  - Chọn rạp
  - Chọn suất chiếu
  - Chọn ghế
  - Thanh toán (ZaloPay)
  - Xem vé đã đặt

- **Tài khoản**
  - Xem thông tin cá nhân
  - Xem lịch sử đặt vé
  - Đánh giá phim đã xem
  - Chỉnh sửa thông tin

## 🛠 Công nghệ sử dụng

- **Frontend**
  - Kotlin
  - Jetpack Compose
  - Material Design 3
  - Coil (Image loading)
  - ZXing (QR/Barcode)

- **Backend**
  - Firebase Authentication
  - Cloud Firestore
  - Firebase Storage
  - Firebase Cloud Messaging

- **Thanh toán**
  - ZaloPay Payment Gateway

## 📱 Màn hình chính

1. **Splash Screen**
   - Logo & loading animation

2. **Login/Register Screen**
   - Form đăng nhập/đăng ký
   - Validation input

3. **Home Screen**
   - Featured movies carousel
   - Now showing movies
   - News & promotions
   - Search bar

4. **Movie Detail Screen**
   - Movie info
   - Trailer
   - Reviews
   - Showtimes
   - Book ticket button

5. **Booking Flow**
   - Select theater
   - Select showtime
   - Select seats
   - Payment
   - Ticket confirmation

6. **Profile Screen**
   - User info
   - Booking history
   - Movie ratings
   - Settings

## 🔧 Cài đặt

1. Clone repository:
```bash
git clone https://github.com/your-username/movie-booking-app.git
```

2. Mở project trong Android Studio

3. Cấu hình Firebase:
   - Tạo project mới trên Firebase Console
   - Tải file `google-services.json`
   - Thêm vào thư mục `app/`

4. Cấu hình ZaloPay:
   - Đăng ký tài khoản ZaloPay Merchant
   - Thêm AppID và MacKey vào `local.properties`

5. Build và chạy ứng dụng

## 📦 Cấu trúc project

```
app/
├── data/
│   ├── model/         # Data classes
│   ├── repository/    # Repository classes
│   └── source/        # Data sources
├── di/               # Dependency injection
├── ui/
│   ├── components/   # Reusable components
│   ├── screens/      # App screens
│   ├── theme/        # App theme
│   └── navigation/   # Navigation
├── utils/            # Utility classes
└── viewmodel/        # ViewModels
```

## 🔐 Bảo mật

- Mã hóa dữ liệu nhạy cảm
- Xác thực người dùng qua Firebase
- Bảo vệ API endpoints
- Secure payment processing với ZaloPay

## 📄 License

```
MIT License

Copyright (c) 2024 Truong Manh Tuan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 👥 Đóng góp

Mọi đóng góp đều được hoan nghênh! Vui lòng:

1. Fork project
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## 📞 Liên hệ

- Email: mtuan74204@gmail.com
- LinkedIn: www.linkedin.com/in/tuantm204
- GitHub: https://github.com/tuantm204

## 🙏 Cảm ơn

Cảm ơn bạn đã quan tâm đến dự án này! Nếu bạn thấy hữu ích, đừng quên:

- ⭐ Star repository
- 👥 Share với bạn bè
- 💬 Đóng góp ý kiến 