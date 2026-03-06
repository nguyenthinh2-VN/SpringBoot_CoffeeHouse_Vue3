# Google Login Integration - Hướng dẫn cấu hình

## 1. Lấy Google OAuth2 Credentials

### Bước 1: Truy cập Google Cloud Console
- Vào https://console.cloud.google.com/
- Tạo project mới hoặc chọn project hiện tại

### Bước 2: Tạo OAuth2 Credentials
1. Vào **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth 2.0 Client ID**
3. Chọn **Web application**
4. Thêm Authorized redirect URIs:
   - `http://localhost:8080/auth/google-login` (Development)
   - `http://localhost:3000/auth/google-login` (Frontend)
   - `https://yourdomain.com/auth/google-login` (Production)
5. Copy **Client ID**

### Bước 3: Enable Google+ API
1. Vào **APIs & Services** → **Library**
2. Tìm **Google+ API**
3. Click **Enable**

## 2. Cấu hình trong ứng dụng

### Thêm vào `config.properties` (hoặc environment variables):
```properties
google.oauth2.client.id=YOUR_GOOGLE_CLIENT_ID_HERE
```

### Hoặc thêm vào environment variables:
```bash
export GOOGLE_OAUTH2_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID_HERE
```

## 3. Sử dụng API

### Endpoint: POST /auth/google-login

**Request:**
```json
{
  "token": "GOOGLE_ID_TOKEN_FROM_CLIENT"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Đăng nhập bằng Google thành công",
  "data": {
    "accessToken": "jwt_access_token",
    "refreshToken": "jwt_refresh_token",
    "expiresIn": 1800,
    "user": {
      "id": 1,
      "email": "user@gmail.com",
      "fullName": "User Name",
      "role": "USER",
      "isActive": true
    }
  }
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Google token không hợp lệ"
}
```

## 4. Frontend Implementation (Vue 3 example)

### Cài đặt Google Sign-In library:
```html
<script src="https://accounts.google.com/gsi/client" async defer></script>
```

### Sử dụng trong Vue 3:
```vue
<script setup>
import { ref } from 'vue'

const handleGoogleLogin = (response) => {
  const token = response.credential
  
  fetch('/auth/google-login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ token })
  })
  .then(res => res.json())
  .then(data => {
    if (data.success) {
      // Lưu token
      localStorage.setItem('accessToken', data.data.accessToken)
      localStorage.setItem('refreshToken', data.data.refreshToken)
      // Redirect to home
      window.location.href = '/'
    } else {
      console.error(data.message)
    }
  })
}

window.onload = () => {
  google.accounts.id.initialize({
    client_id: 'YOUR_GOOGLE_CLIENT_ID',
    callback: handleGoogleLogin
  })
  google.accounts.id.renderButton(
    document.getElementById('googleButton'),
    { theme: 'outline', size: 'large' }
  )
}
</script>

<template>
  <div id="googleButton"></div>
</template>
```

## 5. Luồng hoạt động

1. **Frontend**: User click "Login with Google"
2. **Frontend**: Google Sign-In dialog hiển thị
3. **Frontend**: User xác thực với Google
4. **Frontend**: Nhận Google ID Token
5. **Frontend**: Gửi token đến `/auth/google-login`
6. **Backend**: Xác minh token với Google
7. **Backend**: Kiểm tra email trong database
   - Nếu tồn tại: Trả về JWT tokens
   - Nếu không tồn tại: Tạo user mới với email, bỏ trống username/password
8. **Frontend**: Lưu JWT tokens, redirect to home

## 6. Lưu ý quan trọng

- **Client ID**: Lấy từ Google Cloud Console
- **Token Verification**: Được xác minh bằng Google Auth Library
- **User Creation**: Tự động tạo user nếu email chưa tồn tại
- **Username/Password**: Bỏ trống cho Google login users
- **Full Name**: Lấy từ Google profile nếu có
- **Security**: Token được xác minh trên backend, không tin cậy client

## 7. Troubleshooting

### Lỗi: "Google token không hợp lệ"
- Kiểm tra Client ID có đúng không
- Kiểm tra token có hết hạn không (thường 1 giờ)
- Kiểm tra Authorized redirect URIs trong Google Console

### Lỗi: "Không thể lấy email từ Google token"
- Email có thể bị ẩn trong Google account settings
- Yêu cầu user bật email sharing trong Google account

### Lỗi: "Tài khoản đã bị khóa"
- User đã bị admin khóa
- Liên hệ admin để mở khóa
