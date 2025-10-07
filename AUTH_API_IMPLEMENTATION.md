# API Đăng Ký và Đăng Nhập - Kế Hoạch Implementation

## 📋 Tổng Quan
Tôi sẽ implement hệ thống authentication hoàn chỉnh với JWT (JSON Web Token) cho ứng dụng Coffee House của bạn.

## 🏗️ Kiến Trúc Hệ Thống

### 1. **Models/Entities**
- **User Entity**: Lưu thông tin người dùng
  - `id` (Long): Primary key
  - `username` (String): Tên đăng nhập (unique)
  - `email` (String): Email (unique)
  - `password` (String): Mật khẩu đã hash
  - `role` (Enum): Vai trò (USER, ADMIN)
  - `createdAt` (LocalDateTime): Thời gian tạo
  - `isActive` (Boolean): Trạng thái tài khoản

- **Role Enum**: Định nghĩa các vai trò
  - `USER`: Khách hàng thông thường
  - `ADMIN`: Quản trị viên

### 2. **DTOs (Data Transfer Objects)**
- **RegisterRequest**: Dữ liệu đăng ký
  - `username`, `email`, `password`, `confirmPassword`
- **LoginRequest**: Dữ liệu đăng nhập
  - `username/email`, `password`
- **AuthResponse**: Response sau khi authenticate
  - `token`, `refreshToken`, `userInfo`, `expiresIn`

### 3. **Security Components**
- **JWT Utility**: Tạo và validate JWT tokens
- **Security Config**: Cấu hình Spring Security
- **JWT Filter**: Filter để validate token trong requests
- **Password Encoder**: Mã hóa mật khẩu với BCrypt

## 🔐 JWT Token Strategy

### **Access Token**
- **Thời gian sống**: 15 phút
- **Chứa**: userId, username, role, iat, exp
- **Sử dụng**: Authenticate các API calls

### **Refresh Token**
- **Thời gian sống**: 7 ngày
- **Chứa**: userId, tokenType="refresh", iat, exp
- **Sử dụng**: Tạo access token mới khi hết hạn

## 📡 API Endpoints

### **1. POST /auth/register**
```json
Request:
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!"
}

Response (201):
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com"
  }
}
```

### **2. POST /auth/login**
```json
Request:
{
  "username": "john_doe", // hoặc email
  "password": "SecurePass123!"
}

Response (200):
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900, // 15 minutes in seconds
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "role": "USER"
    }
  }
}
```

### **3. POST /auth/refresh**
```json
Request:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (200):
{
  "success": true,
  "data": {
    "accessToken": "new_access_token...",
    "expiresIn": 900
  }
}
```

### **4. POST /auth/logout**
```json
Request Headers:
Authorization: Bearer <access_token>

Response (200):
{
  "success": true,
  "message": "Đăng xuất thành công"
}
```

### **5. GET /auth/profile**
```json
Request Headers:
Authorization: Bearer <access_token>

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

## 🛡️ Bảo Mật Features

### **1. Password Security**
- **BCrypt hashing**: Mã hóa mật khẩu với salt
- **Password validation**: Ít nhất 8 ký tự, có chữ hoa, chữ thường, số

### **2. JWT Security**
- **Secret key**: Sử dụng strong secret key (256-bit)
- **Token expiration**: Access token ngắn hạn, refresh token dài hạn
- **Token blacklist**: Blacklist tokens khi logout

### **3. Input Validation**
- **Email format**: Validate định dạng email
- **Username uniqueness**: Kiểm tra username đã tồn tại
- **Password strength**: Validate độ mạnh mật khẩu

### **4. Rate Limiting**
- **Login attempts**: Giới hạn số lần đăng nhập sai
- **Registration**: Giới hạn số lần đăng ký từ cùng IP

## 📁 Cấu Trúc Files Sẽ Tạo

```
src/main/java/SpringBoot/demo/
├── Model/
│   ├── User.java
│   ├── Role.java (enum)
│   └── TokenBlacklist.java
├── DTO/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   └── RefreshTokenRequest.java
├── Repository/
│   ├── UserRepository.java
│   └── TokenBlacklistRepository.java
├── Service/
│   ├── AuthService.java
│   ├── UserService.java
│   └── JwtService.java
├── Controller/
│   └── AuthController.java
├── Security/
│   ├── JwtAuthenticationFilter.java
│   ├── SecurityConfig.java
│   └── JwtUtil.java
└── Exception/
    ├── UserAlreadyExistsException.java
    ├── InvalidCredentialsException.java
    └── TokenExpiredException.java
```

## 🔧 Dependencies Cần Thêm

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## 🚀 Implementation Plan & Progress

| Bước | Mô Tả | Hoàn Thành | Chi Tiết |
|------|-------|------------|----------|
| 1 | ✅ Thêm dependencies vào `pom.xml` | ✅ | JWT, Spring Security, Validation |
| 2 | ✅ Tạo User Entity và Repository | ✅ | User.java, Role enum, UserRepository.java |
| 3 | ✅ Tạo DTOs cho request/response | ✅ | RegisterRequest, LoginRequest, AuthResponse |
| 4 | ✅ Implement JWT Utility class | ✅ | JwtUtil.java - tạo và validate tokens |
| 5 | ✅ Tạo AuthService với business logic | ✅ | AuthService.java - register, login logic |
| 6 | ✅ Implement AuthController với các endpoints | ✅ | /auth/register, /auth/login, /auth/refresh |
| 7 | ✅ Cấu hình Spring Security | ✅ | SecurityConfig.java |
| 8 | ✅ Tạo JWT Filter cho authentication | ✅ | JwtAuthenticationFilter.java |
| 9 | ✅ Test các APIs | ✅ | Postman/Thunder Client testing |
| 10 | ⬜ Thêm exception handling và validation | ⬜ | Custom exceptions, input validation |

### 📝 Ghi Chú:
- ✅ = Hoàn thành
- ⏳ = Đang làm (bước tiếp theo)
- ⬜ = Chưa bắt đầu

---

## 🎯 Bước Tiếp Theo: Tạo User Entity và Repository

**Bước 2** sẽ bao gồm:
1. Tạo `Role.java` enum
2. Tạo `User.java` entity với JPA annotations
3. Tạo `UserRepository.java` interface
4. Cập nhật database schema (nếu cần)

## 🧪 Testing Strategy

### **Manual Testing**
- Test đăng ký với các trường hợp: thành công, email/username đã tồn tại
- Test đăng nhập với: thông tin đúng, sai password, user không tồn tại
- Test refresh token: token hợp lệ, token hết hạn, token không hợp lệ
- Test protected endpoints với: token hợp lệ, token hết hạn, không có token

### **Security Testing**
- Test SQL injection trong login form
- Test brute force attacks
- Test token manipulation
- Test CORS configuration

## 💡 Best Practices

1. **Never store passwords in plain text**
2. **Use HTTPS in production**
3. **Implement proper error handling**
4. **Log security events**
5. **Regular token rotation**
6. **Implement account lockout after failed attempts**
7. **Use environment variables for secrets**

---

**Bạn có muốn tôi bắt đầu implement từ bước nào? Tôi khuyến nghị bắt đầu từ việc thêm dependencies và tạo User Entity trước.**
