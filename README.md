# MOVIE APP: HUB FILMS
---
## 📌 Tổng quan dự án (Project Overview)
- Hub Films là một ứng dụng xem phim giải trí, được xây dựng trên nền tảng Java17 và JavaFX. Dự án hướng tới việc tối ưu hóa trải nghiệm xem phim của người dùng và kết nối trực tiếp với kho dữ liệu phim thời gian thực (OphimAPI) qua Rest API.
- Dự án tập trung giải quyết bài toán tối ưu hóa luồng dữ liệu (Data Streaming), quản lý bộ nhớ và trải nghiệm người dùng không độ trễ (Zero-lag UX).
- Giúp người dùng xem và lưu lại lịch sử các bộ phim theo tiến độ, đồng thời lưu lại danh sách phim yêu thích của họ.
---
## 📑 Mục lục (Table of Contents):
* [Tổng quan dự án](#-tổng-quan-dự-án)
* [Công nghệ Backend & Auth](#-công-nghệ-backend--auth)
* [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
* [Tính năng nổi bật](#-tính-năng-nổi-bật)
* [Giao diện ứng dụng](#-giao-diện-ứng-dụng)
---
## 🛠 Công nghệ sử dụng (Tech Stack):
Dự án tập trung vào việc áp dụng các thư viện Java hiện đại để giải quyết các bài toán về mạng, dữ liệu và xử lý bất đồng bộ.

- Core & Architecture:
  - Java 17: 
  - JavaFX: Nền tảng xây dựng giao diện người dùng.
  - MVC + Service + DAO Architecture: Kiến trúc phân lớp chuẩn giúp tách biệt logic nghiệp vụ, quản lý dữ liệu và hiển thị, tạo điều kiện cho việc mở rộng và bảo trì.
    
- Networking & Data Handling:
  - OkHttp 4.x: Được sử dụng để tương tác với RESTful API.
    - Triển khai Connection Pooling để tối ưu hóa hiệu suất kết nối.
    - Cơ chế Automatic Retries giúp ứng dụng hoạt động ổn định trong môi trường mạng không ổn định.
      
  - Jackson Databind: Xử lý chuyển đổi dữ liệu JSON từ API thành các đối tượng Java.
    - Tận dụng cơ chế Java Reflection để tự động hóa việc ánh xạ dữ liệu, giảm thiểu sai sót thủ công.
      
- Persistence & Session Management:
  - JDBC: Lưu thông tin người dùng và danh sách những phim đã xem vào database
  - Singleton Pattern: Áp dụng cho DatabaseConnection và SessionManager để quản lý tập trung phiên làm việc của người dùng và các kết nối cơ sở dữ liệu nặng.

- Concurrency (Xử lý đa luồng):
  - JavaFX Task & Service: Giải quyết bài toán Asynchronous Data Fetching.
---
## 🏗️ Kiến trúc hệ thống (System Architecture):
```text
movieapp/
├── src/
│   └── main/
│       ├── java/com/myapp/
│       │   ├── controller/                # Điều phối hành vi & tương tác người dùng
│       │   │   ├── HomeController.java
│       │   │   ├── LoginController.java
│       │   │   ├── Regiscontroller.java
│       │   │   ├── WatchController.java
|       |   |   └── ...
│       │   ├── dao/                       # Tầng giao tiếp dữ liệu (API & Database)
│       │   │   ├── HistoryDAO.java
│       │   │   ├── OphimApiClient.java
│       │   │   └── UserDAO.java
│       │   ├── exception/
│       │   │   ├── AuthException
│       │   │   ├── OtpException
│       │   │   └── ...
│       │   ├── service/                   # Xử lý logic nghiệp vụ lõi
│       │   │   ├── AuthService.java
│       │   │   ├── EmailService.java
│       │   │   ├── OtpService.java
│       │   │   └── MovieService.java
│       │   ├── model/                     # Các đối tượng dữ liệu (POJOs)
│       │   │   ├── Movie.java
│       │   │   ├── ApiResponse.java
│       │   │   ├── Episode.java
│       │   │   ├── MovieDetail.java
│       │   │   ├── User.java
│       │   │   └── ...
│       │   ├── util/                      # Công cụ hỗ trợ (Session, Navigation)
│       │   │   ├── DBConnection.java
│       │   │   ├── OtpGenerationUtil.java
│       │   │   ├── SceneNavigator.java
│       │   │   ├── UIUtils.java
│       │   │   └── ...
│       │   └── MainApp.java
│       └── resources/
│           ├── css/                      # Định dạng phong cách (Stylesheets)
│           ├── images/                   # Tài nguyên hình ảnh (Icons, Logo)
│           └── fxml (view)/              # Giao diện người dùng (XML)
│               ├── home.fxml
│               ├── watch.fxml
│               ├── login.fxml
│               └── ...
└── pom.xml                               # Cấu hình Maven & Dependencies
```
---
## 🌟 Tính năng nổi bật (Key Features):
- Đăng nhập, đăng kí, có gửi otp về Email (chào mừng, xác thực để đặt lại mật khẩu khi quên). Sử dụng Pattern Singleton để duy trì trạng thái đăng nhập của người dùng xuyên suốt các màn hình.
- Tự động cập nhật kho phim khổng lồ từ nguồn API của OphimAPI.
- Cá nhân hóa: Lưu lịch sử xem của người dùng và cập nhật theo thời gian thực.
- Xem phim, tua video, chỉnh tốc độ phát.
---
## 📺 Giao diện ứng dụng (App Screenshots)
