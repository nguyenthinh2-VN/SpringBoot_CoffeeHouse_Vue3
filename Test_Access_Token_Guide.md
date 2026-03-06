# 🧪 Hướng Dẫn Test Authentication API

## 📋 Tổng Quan
File này hướng dẫn cách test tất cả các API authentication của Coffee House application.

---

## 🚀 Bước 1: Test Đăng Ký (Register)

### **API Endpoint**
```http
POST http://localhost:3000/auth/register
Content-Type: application/json
```

### **Request Body**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "confirmPassword": "password123"
}
```

### **Expected Response (Success)**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "USER"
  }
}
```

### **Test Cases**
- ✅ **Thành công**: Tất cả thông tin hợp lệ
- ❌ **Username đã tồn tại**: Sử dụng username đã có
- ❌ **Email đã tồn tại**: Sử dụng email đã có
- ❌ **Password không khớp**: confirmPassword khác password
- ❌ **Validation lỗi**: Email sai format, password quá ngắn

---

## 🔐 Bước 2: Test Đăng Nhập (Login)

### **API Endpoint**
```http
POST http://localhost:3000/auth/login
Content-Type: application/json
```

### **Request Body**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

### **Expected Response (Success)**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "role": "USER"
    }
  }
}
```

### **Test Cases**
- ✅ **Thành công**: Username/password đúng
- ❌ **Sai password**: Password không đúng
- ❌ **User không tồn tại**: Username/email không có trong DB
- ❌ **Account bị khóa**: isActive = false

**📝 Lưu ý**: Lưu lại `accessToken` và `refreshToken` từ response để test các bước tiếp theo!

---

## 👤 Bước 3: Test Truy Cập Profile (Protected Route)

### **API Endpoint**
```http
GET http://localhost:3000/auth/profile
Authorization: Bearer <ACCESS_TOKEN_FROM_LOGIN>
```

### **Expected Response (Success)**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

---

## 🔄 Bước 4: Test Refresh Token

### **API Endpoint**
```http
POST http://localhost:3000/auth/refresh
Content-Type: application/json
```

### **Request Body**
```json
{
  "refreshToken": "<REFRESH_TOKEN_FROM_LOGIN>"
}
```

### **Expected Response (Success)**
```json
{
  "success": true,
  "data": {
    "accessToken": "new_access_token...",
    "expiresIn": 1800
  }
}
```

---

## 🚪 Bước 5: Test Đăng Xuất (Logout)

### **API Endpoint**
```http
POST http://localhost:3000/auth/logout
Authorization: Bearer <ACCESS_TOKEN>
```

### **Expected Response (Success)**
```json
{
  "success": true,
  "message": "Đăng xuất thành công"
}
```

---

## ❌ Test Các Trường Hợp Lỗi Token

### **1. Không có Token**
```http
GET http://localhost:3000/auth/profile
# Không có Authorization header
```
**Expected**: `401 Unauthorized` - "Token không được cung cấp"

### **2. Token Sai Format**
```http
GET http://localhost:3000/auth/profile
Authorization: Bearer invalid_token_format
```
**Expected**: `401 Unauthorized` - "Token không đúng định dạng"

### **3. Token Hết Hạn**
```http
GET http://localhost:3000/auth/profile
Authorization: Bearer <expired_token>
```
**Expected**: `401 Unauthorized` - "Token đã hết hạn"

### **4. Token Signature Sai**
```http
GET http://localhost:3000/auth/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.signature
```
**Expected**: `401 Unauthorized` - "Chữ ký token không hợp lệ"

### **5. Sử dụng Refresh Token cho Access Token**
```http
GET http://localhost:3000/auth/profile
Authorization: Bearer <refresh_token>
```
**Expected**: `401 Unauthorized` - "Token type không hợp lệ"

---

## 🛠️ Tools để Test

### **1. Postman**
- Import các request trên vào Postman
- Tạo Environment variables cho `baseUrl`, `accessToken`, `refreshToken`

### **2. Thunder Client (VS Code Extension)**
- Tạo collection cho Authentication APIs
- Sử dụng variables để lưu tokens

### **3. cURL Commands**
```bash
# Register
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","confirmPassword":"password123"}'

# Login
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Profile
curl -X GET http://localhost:3000/auth/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 📊 Checklist Test

- [ ] **Register**: Thành công với thông tin hợp lệ
- [ ] **Register**: Lỗi khi username/email trùng
- [ ] **Login**: Thành công với thông tin đúng
- [ ] **Login**: Lỗi khi sai password
- [ ] **Profile**: Truy cập thành công với token hợp lệ
- [ ] **Profile**: Lỗi khi không có token
- [ ] **Profile**: Lỗi khi token hết hạn
- [ ] **Profile**: Lỗi khi token sai format
- [ ] **Refresh**: Thành công với refresh token hợp lệ
- [ ] **Refresh**: Lỗi khi refresh token hết hạn
- [ ] **Logout**: Thành công với access token hợp lệ

---

## 🔄 Flow Test Hoàn Chỉnh

1. **Đăng ký** user mới
2. **Đăng nhập** với user vừa tạo → Lưu tokens
3. **Truy cập profile** với access token
4. **Đợi token hết hạn** hoặc test với expired token
5. **Refresh token** để lấy access token mới
6. **Truy cập profile** với token mới
7. **Đăng xuất** để blacklist token
8. **Thử truy cập profile** với token đã logout (should fail)

**🎯 Mục tiêu**: Tất cả test cases phải pass để đảm bảo security hoạt động đúng!
