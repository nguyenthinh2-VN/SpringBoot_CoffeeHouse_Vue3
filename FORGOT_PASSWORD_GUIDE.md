# Hướng dẫn sử dụng chức năng Quên mật khẩu

## Tổng quan
Chức năng quên mật khẩu cho phép người dùng đặt lại mật khẩu thông qua mã OTP được gửi qua email.

## Quy trình
1. **Gửi OTP**: Người dùng nhập email, hệ thống gửi mã OTP 6 số
2. **Xác minh OTP**: Người dùng nhập mã OTP để xác thực
3. **Đặt lại mật khẩu**: Sau khi xác minh thành công, người dùng có thể đặt mật khẩu mới

## Cấu hình Email với Resend API

### ✅ Khuyến nghị: Sử dụng Resend (Đơn giản, Nhanh, Ổn định)

**Tại sao dùng Resend?**
- Không cần cấu hình SMTP phức tạp
- Không bị firewall chặn
- Free: 100 emails/ngày, 3,000 emails/tháng
- Email HTML đẹp với styling

**Cấu hình:**

1. **Đăng ký Resend**: https://resend.com
2. **Lấy API Key**: Dashboard → API Keys → Create API Key
3. **Cập nhật application.properties**:
```properties
resend.api.key=re_xxxxxxxxxxxxxxxxxxxxxxxxxx
resend.from.email=onboarding@resend.dev
```

📖 **Xem hướng dẫn chi tiết:** [RESEND_SETUP_GUIDE.md](./RESEND_SETUP_GUIDE.md)

---

### Phương án 2: SMTP (Gmail, Outlook, Yahoo)

Nếu muốn dùng SMTP truyền thống:

#### Gmail
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Lưu ý:** Cần tạo App Password tại https://myaccount.google.com/security

#### Outlook/Hotmail
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
```

#### Yahoo Mail
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=your-email@yahoo.com
spring.mail.password=your-app-password
```

## API Endpoints

### 1. POST /auth/forgot-password
Gửi mã OTP đến email

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.",
  "data": null
}
```

**Response Error (400):**
```json
{
  "success": false,
  "message": "Email không tồn tại trong hệ thống",
  "data": null
}
```

---

### 2. POST /auth/verify-otp
Xác minh mã OTP

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Mã OTP hợp lệ. Bạn có thể đặt lại mật khẩu.",
  "data": null
}
```

**Response Error (400):**
```json
{
  "success": false,
  "message": "Mã OTP không hợp lệ hoặc đã hết hạn",
  "data": null
}
```

---

### 3. POST /auth/reset-password
Đặt lại mật khẩu mới

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.",
  "data": null
}
```

**Response Error (400):**
```json
{
  "success": false,
  "message": "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.",
  "data": null
}
```

## Kiểm tra với Swagger UI

1. Khởi động ứng dụng
2. Truy cập: http://localhost:3000/swagger-ui.html
3. Tìm section "Authentication"
4. Test các endpoint theo thứ tự:
   - `/auth/forgot-password` - Gửi OTP
   - `/auth/verify-otp` - Xác minh OTP (tùy chọn)
   - `/auth/reset-password` - Đặt lại mật khẩu

## Kiểm tra với cURL

### 1. Gửi OTP
```bash
curl -X POST http://localhost:3000/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'
```

### 2. Xác minh OTP
```bash
curl -X POST http://localhost:3000/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "otp": "123456"}'
```

### 3. Đặt lại mật khẩu
```bash
curl -X POST http://localhost:3000/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "otp": "123456", "newPassword": "newpass123"}'
```

## Lưu ý quan trọng

### Bảo mật
- ✅ Mã OTP có thời hạn 10 phút
- ✅ Mỗi OTP chỉ sử dụng được 1 lần
- ✅ Mật khẩu được mã hóa bằng BCrypt
- ✅ Không cần đăng nhập để đặt lại mật khẩu

### Xử lý lỗi
- Email không tồn tại → Thông báo lỗi
- OTP sai → Thông báo lỗi
- OTP hết hạn → Yêu cầu gửi lại OTP mới
- Tài khoản bị khóa → Không cho phép đặt lại mật khẩu

### Database
Bảng `password_reset_tokens` được tạo tự động với các trường:
- `id`: Primary key
- `email`: Email người dùng
- `otp`: Mã OTP 6 số
- `expiry_date`: Thời gian hết hạn (10 phút)
- `is_used`: Đánh dấu đã sử dụng
- `created_at`: Thời gian tạo

## Troubleshooting

### Lỗi: "Không thể gửi email"
**Nguyên nhân:**
- Sai cấu hình email
- Chưa tạo App Password (Gmail)
- Firewall chặn port 587

**Giải pháp:**
1. Kiểm tra lại username/password trong `application.properties`
2. Đảm bảo đã tạo App Password cho Gmail
3. Kiểm tra kết nối mạng và firewall

### Lỗi: "Mã OTP đã hết hạn"
**Nguyên nhân:**
- OTP hết hạn sau 10 phút

**Giải pháp:**
- Gửi lại OTP mới bằng endpoint `/auth/forgot-password`

### Lỗi: "Mã OTP không hợp lệ"
**Nguyên nhân:**
- Nhập sai mã OTP
- OTP đã được sử dụng

**Giải pháp:**
- Kiểm tra lại email và nhập đúng mã
- Nếu đã dùng, gửi lại OTP mới

## Tùy chỉnh

### Thay đổi thời gian hết hạn OTP
Mở file `PasswordResetToken.java`:
```java
public PasswordResetToken() {
    this.createdAt = LocalDateTime.now();
    this.expiryDate = LocalDateTime.now().plusMinutes(15); // Đổi từ 10 thành 15 phút
}
```

### Thay đổi độ dài OTP
Mở file `PasswordResetService.java`:
```java
private String generateOtp() {
    int otp = 1000 + random.nextInt(9000); // OTP 4 số
    return String.valueOf(otp);
}
```

### Tùy chỉnh nội dung email
Mở file `EmailService.java` và sửa method `buildEmailContent()`.

## Tích hợp Frontend

### Ví dụ với Vue 3 + Axios

```javascript
// 1. Gửi OTP
async function sendOtp(email) {
  try {
    const response = await axios.post('/auth/forgot-password', { email });
    alert(response.data.message);
  } catch (error) {
    alert(error.response.data.message);
  }
}

// 2. Xác minh OTP (tùy chọn)
async function verifyOtp(email, otp) {
  try {
    const response = await axios.post('/auth/verify-otp', { email, otp });
    return response.data.success;
  } catch (error) {
    alert(error.response.data.message);
    return false;
  }
}

// 3. Đặt lại mật khẩu
async function resetPassword(email, otp, newPassword) {
  try {
    const response = await axios.post('/auth/reset-password', {
      email,
      otp,
      newPassword
    });
    alert(response.data.message);
    // Chuyển về trang login
    router.push('/login');
  } catch (error) {
    alert(error.response.data.message);
  }
}
```

## Kết luận

Chức năng quên mật khẩu đã được triển khai đầy đủ với:
- ✅ Gửi OTP qua email
- ✅ Xác minh OTP
- ✅ Đặt lại mật khẩu
- ✅ Bảo mật với thời gian hết hạn
- ✅ Validation đầy đủ
- ✅ Tích hợp Swagger UI

Hãy đảm bảo cấu hình email đúng trước khi sử dụng!
