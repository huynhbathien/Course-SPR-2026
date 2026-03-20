# Course Application — Spring Boot

Backend REST API cho hệ thống quản lý khóa học trực tuyến, xây dựng bằng **Spring Boot 3.4 + Java 21**.

---

## Tech Stack

| Layer            | Technology                                        |
| ---------------- | ------------------------------------------------- |
| Language         | Java 21                                           |
| Framework        | Spring Boot 3.4.4                                 |
| Security         | Spring Security 6.4 + JWT (HS512) + OAuth2 Google |
| Database         | MySQL 8 + Spring Data JPA                         |
| Cache            | Redis 7 (Jedis)                                   |
| Rate Limiting    | Bucket4j                                          |
| Mapping          | MapStruct 1.5                                     |
| Email            | Spring Mail (Gmail SMTP)                          |
| Containerization | Docker + Docker Compose                           |
| Testing          | JUnit 5 + Mockito + Spring Security Test          |

---

## Tính năng

### Authentication & Authorization

- Đăng ký / Đăng nhập bằng username + password
- OAuth2 login qua Google
- JWT Access Token (30 phút) + Refresh Token (7 ngày) lưu trong HttpOnly cookie
- OTP xác thực email khi đăng ký
- Forgot password / Reset password qua OTP email
- Rate limiting chống brute-force (Bucket4j)
- Blacklist token bằng Redis khi logout

### Khóa học

- CRUD khóa học (ADMIN)
- Tìm kiếm theo keyword + filter theo loại khóa học (`typeCode`)
- Phân trang, sắp xếp
- Nhóm khóa học theo `CourseType`

### Bài học

- CRUD bài học (ADMIN)
- Lesson dependency (bài học yêu cầu hoàn thành bài trước)
- Đánh dấu hoàn thành bài học, theo dõi tiến độ
- Tìm kiếm bài học có phân trang

### Người dùng

- Xem thông tin cá nhân
- Xem thông tin người dùng khác (authenticated)

### Admin Dashboard

- Quản lý users: xem danh sách, bật/tắt tài khoản, thay đổi role
- Quản lý courses: publish / unpublish
- Thống kê tổng quan: user, course, enrollment

---

## Cấu trúc dự án

```
src/main/java/com/mycompany/
├── config/          # SecurityConfig, CorsConfig, RedisConfig, JwtProperties…
├── controller/      # AuthController, CourseController, LessonController, AdminController, UserController
├── dto/
│   ├── request/     # LoginRequestDTO, RegisterRequestDTO, CourseRequest…
│   └── response/    # CourseResponse, LessonResponse, AdminStatsResponse…
├── entity/          # UserEntity, Course, Lesson, UserCourse, UserLesson, OtpEntity, CourseType
├── enums/           # EnumRole, EnumCourseStatus, EnumAuthError, EnumSuccess…
├── exception/       # GlobalExceptionHandler, AppException…
├── mapstruct/       # CourseMapper, LessonMapper, UserMapper…
├── repository/      # CourseRepository, LessonRepository, UserRepository…
├── security/        # JwtUtils, JwtAuthenticationFilter, OAuth2LoginSuccessHandler, RateLimitFilter…
├── service/
│   ├── email/       # OtpEmailTemplateStrategy, VerifyEmailTemplateStrategy, ResetPasswordEmailTemplateStrategy
│   └── Impl/        # AuthServiceImpl, CourseServiceImpl, LessonServiceImpl, EmailServiceImpl, SmtpEmailSenderServiceImpl…
└── util/            # QueryUtils, RequestUtils…
```

---

## Cài đặt & Chạy

### Yêu cầu

- Java 21+
- Maven 3.9+
- MySQL 8 (port 3306)
- Redis 7 (port 6379)

### Local Development

**1. Clone và tạo file `.env`:**

```bash
git clone https://github.com/huynhbathien/Course-SPR-2026.git
cd Course-SPR-2026
cp .env.example .env   # Điền đầy đủ thông tin
```

**2. Nội dung `.env` (local):**

```dotenv
# Google OAuth2
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
GOOGLE_REDIRECT_URI=https://localhost:8080/login/oauth2/code/google

# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/course_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your-password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=

# JWT
JWT_SECRET_FILE=secrets/jwt_hs512_key.txt

# Token expiration (seconds)
TOKEN_ACCESS_EXPIRATION=1800
TOKEN_REFRESH_EXPIRATION=604800

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# SSL
SSL_KEY_STORE=file:secrets/keystore.p12
SSL_KEY_STORE_PASSWORD=your-keystore-password
SSL_KEY_ALIAS=tomcat

# OTP
OTP_EXPIRY_MINUTES=10
```

**3. Chạy:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker (Production)

**1. Tạo secret files:**

```bash
echo -n "your-jwt-key"          > secrets/jwt_hs512_key.txt
echo -n "db-username"           > secrets/mysql_user.txt
echo -n "db-password"           > secrets/mysql_password.txt
echo -n "db-root-password"      > secrets/mysql_root_password.txt
echo -n "redis-password"        > secrets/redis_password.txt
echo -n "keystore-password"     > secrets/ssl_keystore_password.txt
echo -n "google-client-secret"  > secrets/google_client_secret.txt
# Đặt keystore.p12 vào secrets/keystore.p12
```

**2. Tạo `.env` (production):**

```dotenv
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

**3. Chạy:**

```bash
docker compose up -d
```

---

## API Endpoints

### Auth — `/api/auth`

| Method | Endpoint                | Mô tả                  |
| ------ | ----------------------- | ---------------------- |
| POST   | `/auth/login`           | Đăng nhập              |
| POST   | `/auth/register`        | Đăng ký                |
| POST   | `/auth/verify-email`    | Xác thực OTP email     |
| POST   | `/auth/resend-otp`      | Gửi lại OTP            |
| POST   | `/auth/forgot-password` | Yêu cầu reset mật khẩu |
| POST   | `/auth/reset-password`  | Đặt lại mật khẩu       |
| POST   | `/auth/refresh`         | Làm mới access token   |
| POST   | `/auth/logout`          | Đăng xuất              |

### Course — `/api/course`

| Method | Endpoint                        | Auth  | Mô tả                    |
| ------ | ------------------------------- | ----- | ------------------------ |
| GET    | `/course/{id}`                  | User  | Chi tiết khóa học        |
| GET    | `/course/list`                  | User  | Danh sách nhóm theo type |
| GET    | `/course/search?keyword=&type=` | User  | Tìm kiếm + filter        |
| POST   | `/course`                       | Admin | Tạo khóa học             |
| PUT    | `/course/{id}`                  | Admin | Cập nhật khóa học        |
| DELETE | `/course/{id}`                  | Admin | Xóa khóa học             |

### Lesson — `/api/lesson`

| Method | Endpoint                                      | Auth  | Mô tả                   |
| ------ | --------------------------------------------- | ----- | ----------------------- |
| GET    | `/lesson/{id}`                                | User  | Chi tiết bài học        |
| GET    | `/lesson/course/{courseId}`                   | User  | Bài học theo khóa học   |
| GET    | `/lesson/search?keyword=`                     | User  | Tìm kiếm bài học        |
| POST   | `/lesson/{userId}/lesson/{lessonId}/complete` | User  | Hoàn thành bài học      |
| GET    | `/lesson/user/{userId}/completed`             | User  | Danh sách đã hoàn thành |
| GET    | `/lesson/user/{userId}/active`                | User  | Danh sách đang học      |
| POST   | `/lesson`                                     | Admin | Tạo bài học             |
| PUT    | `/lesson/{id}`                                | Admin | Cập nhật bài học        |
| DELETE | `/lesson/{id}`                                | Admin | Xóa bài học             |

### Admin — `/api/admin`

| Method | Endpoint                        | Mô tả                                |
| ------ | ------------------------------- | ------------------------------------ |
| GET    | `/admin/users`                  | Danh sách users (phân trang, filter) |
| GET    | `/admin/users/{id}`             | Chi tiết user                        |
| PUT    | `/admin/users/{id}/status`      | Bật/tắt tài khoản                    |
| PUT    | `/admin/users/{id}/role`        | Thay đổi role                        |
| GET    | `/admin/courses`                | Danh sách courses (phân trang)       |
| PUT    | `/admin/courses/{id}/publish`   | Publish khóa học                     |
| PUT    | `/admin/courses/{id}/unpublish` | Unpublish khóa học                   |
| GET    | `/admin/stats`                  | Thống kê tổng quan                   |

---

## Security

- Tất cả secrets trong production được quản lý qua **Docker Secrets** (không hardcode trong env)
- `MAIL_USERNAME`, `MAIL_PASSWORD`, `GOOGLE_CLIENT_SECRET` đọc từ **env var** (`.env` local / host env production)
- JWT key, DB credentials, Redis password, SSL keystore dùng **Docker Secrets** (`/run/secrets/`)
- File `.env` và `secrets/` đã được **gitignore**
- HTTPS enabled với PKCS12 keystore
- CORS configured
- CSRF disabled (stateless JWT API)

---

## Testing

```bash
mvn test
```

103 unit tests bao gồm:

- `AuthServiceImplTest`
- `CourseServiceImplTest`
- `LessonServiceImplTest`
- `UserServiceImplTest`
- `AdminServiceImplTest`
- `EmailServiceImplTest`
- `OtpServiceImplTest`

### Chạy riêng test email

Unit test (mock sender):

```bash
mvn -q -Dtest=EmailServiceImplTest test
```

Live send test (gửi mail thật):

```bash
mvn -q -Dtest=EmailServiceLiveSendTest test
```

Ghi chú live test:

- Test sẽ ưu tiên đọc `MAIL_*` từ biến môi trường hệ điều hành
- Nếu không có, test sẽ fallback đọc từ file `.env` ở root project
- Cần có tối thiểu `MAIL_USERNAME` và `MAIL_PASSWORD`
- Target hiện tại: `huynhbathien123456@gmail.com`

---

## Branch Strategy

| Branch     | Mục đích                                                     |
| ---------- | ------------------------------------------------------------ |
| `main`     | Production-ready, chỉ merge từ `develop` sau khi test đầy đủ |
| `develop`  | Integration branch, base để merge tất cả feature branches    |
| `feat/*`   | Feature branches                                             |
| `hotfix/*` | Hotfix branches                                              |

---

## Architecture Refactor Plan

- Xem roadmap SOLID chi tiet tai [docs/SOLID-ROADMAP.md](docs/SOLID-ROADMAP.md)
