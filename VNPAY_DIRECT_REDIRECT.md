# VNPAY Direct Redirect to Frontend

## Thay đổi

VNPAY callback sẽ redirect **trực tiếp** tới Frontend Vue 3 thay vì qua Backend:

### Trước (qua Backend)
```
VNPAY → Backend (/orders/vnpay-callback) → Frontend
```

### Sau (trực tiếp)
```
VNPAY → Frontend (/payment-success)
```

## Config

**File: `application.properties`**
```properties
vnpay.returnUrl=${vnpay.return.url:http://localhost:5173/payment-success}
```

## Frontend Implementation (Vue 3)

### 1. Tạo component PaymentSuccess

**File: `src/views/PaymentSuccess.vue`**

```vue
<template>
  <div class="payment-success-container">
    <div class="success-card">
      <div class="success-icon">
        <i class="fas fa-check-circle"></i>
      </div>
      
      <h1>Thanh Toán Thành Công!</h1>
      <p class="message">Đơn hàng của bạn đã được xác nhận</p>
      
      <div class="order-info" v-if="orderInfo">
        <p><strong>Mã đơn hàng:</strong> {{ orderInfo }}</p>
        <p><strong>Trạng thái:</strong> Đã thanh toán</p>
        <p><strong>Thời gian:</strong> {{ currentTime }}</p>
      </div>
      
      <div class="actions">
        <router-link to="/orders" class="btn btn-primary">
          <i class="fas fa-box"></i> Xem Đơn Hàng
        </router-link>
        <router-link to="/home" class="btn btn-secondary">
          <i class="fas fa-home"></i> Quay Về Trang Chủ
        </router-link>
      </div>

      <div class="timer">
        Tự động chuyển hướng sau <strong>{{ countdown }}</strong> giây...
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const orderInfo = ref('')
const currentTime = ref('')
const countdown = ref(10)

onMounted(() => {
  // Lấy thông tin từ VNPAY callback params
  const vnpOrderInfo = route.query.vnp_OrderInfo
  const vnpResponseCode = route.query.vnp_ResponseCode
  
  if (vnpOrderInfo) {
    // Decode URL encoded string
    orderInfo.value = decodeURIComponent(vnpOrderInfo)
  }

  // Kiểm tra response code
  if (vnpResponseCode !== '00') {
    console.warn('Payment response code:', vnpResponseCode)
    // Có thể redirect tới error page nếu cần
  }

  // Hiển thị thời gian hiện tại
  const now = new Date()
  currentTime.value = now.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })

  // Lưu trạng thái thanh toán
  localStorage.setItem('lastPaymentStatus', JSON.stringify({
    orderInfo: orderInfo.value,
    status: 'success',
    timestamp: new Date().toISOString(),
    vnpayParams: route.query
  }))

  // Countdown timer
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      router.push('/home')
    }
  }, 1000)
})
</script>

<style scoped>
.payment-success-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.success-card {
  background: white;
  border-radius: 15px;
  padding: 50px 40px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  width: 100%;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.success-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 30px;
  animation: scaleIn 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.success-icon i {
  font-size: 50px;
  color: white;
}

h1 {
  color: #333;
  font-size: 32px;
  margin-bottom: 10px;
  font-weight: 700;
}

.message {
  color: #666;
  font-size: 18px;
  margin-bottom: 30px;
}

.order-info {
  background: #f8f9fa;
  border-left: 4px solid #667eea;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  text-align: left;
}

.order-info p {
  color: #666;
  margin: 10px 0;
}

.order-info strong {
  color: #333;
}

.actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.btn {
  padding: 14px 32px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  font-size: 16px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  border: 2px solid #ddd;
}

.btn-secondary:hover {
  background: #e8e8e8;
  border-color: #999;
  transform: translateY(-3px);
}

.timer {
  color: #999;
  font-size: 14px;
}

.timer strong {
  color: #667eea;
  font-weight: 700;
}

@media (max-width: 600px) {
  .success-card {
    padding: 30px 20px;
  }

  h1 {
    font-size: 24px;
  }

  .actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
```

### 2. Thêm route

**File: `src/router/index.js`**

```javascript
import PaymentSuccess from '@/views/PaymentSuccess.vue'

const routes = [
  // ... existing routes
  {
    path: '/payment-success',
    name: 'PaymentSuccess',
    component: PaymentSuccess
  }
]
```

## VNPAY Callback Parameters

VNPAY sẽ gửi các params này:

```
vnp_Amount          - Số tiền
vnp_BankCode        - Mã ngân hàng
vnp_BankTranNo      - Mã giao dịch ngân hàng
vnp_CardType        - Loại thẻ
vnp_OrderInfo       - Thông tin đơn hàng (mã đơn)
vnp_PayDate         - Ngày thanh toán
vnp_ResponseCode    - Mã phản hồi (00 = thành công)
vnp_TmnCode         - Mã merchant
vnp_TransactionNo   - Mã giao dịch VNPAY
vnp_TransactionStatus - Trạng thái giao dịch
vnp_TxnRef          - Mã tham chiếu giao dịch
vnp_SecureHash      - Mã bảo mật
```

## Luồng hoàn chỉnh

```
1. User click "Thanh Toán"
   ↓
2. Frontend gọi POST /orders/{orderId}/vnpay-payment
   ↓
3. Backend trả VNPAY payment link
   ↓
4. Frontend redirect tới VNPAY
   ↓
5. User thanh toán trên VNPAY
   ↓
6. VNPAY redirect → http://localhost:5173/payment-success?vnp_*=...
   ↓
7. Frontend hiển thị trang thành công
```

## Cấu hình Environment

**Development (.env.local)**
```
VITE_API_URL=http://localhost:3000
```

**Production (.env.production)**
```
VITE_API_URL=https://api.yourdomain.com
```

## Lưu ý

- Port 5173 là Vite dev server mặc định
- Thay đổi port nếu dùng port khác
- Trong production, thay `localhost:5173` bằng domain thực tế
- VNPAY config cần được cập nhật tương ứng
