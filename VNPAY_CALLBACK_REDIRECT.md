# VNPAY Callback Redirect - Hướng dẫn

## Thay đổi
Sau khi thanh toán VNPAY xong, thay vì trả JSON, backend sẽ **redirect** tới trang Frontend:

### Success Case
```
Backend → Redirect → http://localhost:3000/payment-success?orderId=123
```

### Error Case
```
Backend → Redirect → http://localhost:3000/payment-error?message=Lỗi+thanh+toán
```

## Frontend Implementation (Vue 3)

### 1. Tạo trang Payment Success

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
      
      <div class="order-info" v-if="orderId">
        <p><strong>Mã đơn hàng:</strong> #{{ orderId }}</p>
        <p><strong>Trạng thái:</strong> Đã thanh toán</p>
      </div>
      
      <div class="actions">
        <router-link to="/orders" class="btn btn-primary">
          Xem Đơn Hàng
        </router-link>
        <router-link to="/home" class="btn btn-secondary">
          Quay Về Trang Chủ
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const orderId = ref(null)

onMounted(() => {
  orderId.value = route.query.orderId
  // Optional: Gọi API để lấy chi tiết đơn hàng
  if (orderId.value) {
    fetchOrderDetails()
  }
})

const fetchOrderDetails = async () => {
  try {
    const response = await fetch(`/orders/${orderId.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      }
    })
    const data = await response.json()
    if (data.success) {
      console.log('Order details:', data.data)
    }
  } catch (error) {
    console.error('Error fetching order:', error)
  }
}
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
  border-radius: 10px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 100%;
}

.success-icon {
  font-size: 80px;
  color: #4caf50;
  margin-bottom: 20px;
  animation: scaleIn 0.5s ease-in-out;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

h1 {
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
}

.message {
  color: #666;
  margin-bottom: 30px;
  font-size: 16px;
}

.order-info {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  text-align: left;
}

.order-info p {
  margin: 10px 0;
  color: #555;
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn {
  padding: 12px 30px;
  border-radius: 5px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-block;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
  transform: translateY(-2px);
}

.btn-secondary {
  background: #e0e0e0;
  color: #333;
}

.btn-secondary:hover {
  background: #d0d0d0;
  transform: translateY(-2px);
}
</style>
```

### 2. Tạo trang Payment Error

**File: `src/views/PaymentError.vue`**

```vue
<template>
  <div class="payment-error-container">
    <div class="error-card">
      <div class="error-icon">
        <i class="fas fa-times-circle"></i>
      </div>
      
      <h1>Thanh Toán Thất Bại!</h1>
      <p class="message">{{ errorMessage || 'Có lỗi xảy ra trong quá trình thanh toán' }}</p>
      
      <div class="actions">
        <router-link to="/checkout" class="btn btn-primary">
          Thử Lại
        </router-link>
        <router-link to="/home" class="btn btn-secondary">
          Quay Về Trang Chủ
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const errorMessage = ref('')

onMounted(() => {
  errorMessage.value = route.query.message || 'Thanh toán thất bại'
})
</script>

<style scoped>
.payment-error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 20px;
}

.error-card {
  background: white;
  border-radius: 10px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 100%;
}

.error-icon {
  font-size: 80px;
  color: #f44336;
  margin-bottom: 20px;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}

h1 {
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
}

.message {
  color: #666;
  margin-bottom: 30px;
  font-size: 16px;
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn {
  padding: 12px 30px;
  border-radius: 5px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-block;
}

.btn-primary {
  background: #f5576c;
  color: white;
}

.btn-primary:hover {
  background: #e63946;
  transform: translateY(-2px);
}

.btn-secondary {
  background: #e0e0e0;
  color: #333;
}

.btn-secondary:hover {
  background: #d0d0d0;
  transform: translateY(-2px);
}
</style>
```

### 3. Thêm Routes

**File: `src/router/index.js`**

```javascript
import PaymentSuccess from '@/views/PaymentSuccess.vue'
import PaymentError from '@/views/PaymentError.vue'

const routes = [
  // ... existing routes
  {
    path: '/payment-success',
    name: 'PaymentSuccess',
    component: PaymentSuccess
  },
  {
    path: '/payment-error',
    name: 'PaymentError',
    component: PaymentError
  }
]
```

## Luồng Thanh Toán Hoàn Chỉnh

```
1. User chọn sản phẩm → Tạo đơn hàng
2. User click "Thanh Toán" → GET /orders/{orderId}/vnpay-payment
3. Backend trả VNPAY link
4. User redirect tới VNPAY
5. User thanh toán trên VNPAY
6. VNPAY callback → GET /orders/vnpay-callback?vnp_*
7. Backend xác minh + cập nhật order status
8. Backend redirect → http://localhost:3000/payment-success?orderId=123
9. Frontend hiển thị trang thành công
```

## Cấu Hình CORS

Đảm bảo CORS đã được cấu hình để cho phép redirect:

**SecurityConfig.java** (đã có sẵn)
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000", 
    "http://localhost:5173", 
    "http://localhost:8080"
));
```

## Lưu Ý

- Thay `localhost:3000` bằng domain thực tế trong production
- Lưu orderId để có thể fetch chi tiết đơn hàng
- Thêm loading state khi fetch order details
- Xử lý case khi không có orderId trong URL
