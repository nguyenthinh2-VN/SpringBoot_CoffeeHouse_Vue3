# 📋 KẾ HOẠCH TRIỂN KHAI CHỨC NĂNG ĐƠN HÀNG (ORDER MANAGEMENT)

## 📊 TỔNG QUAN HỆ THỐNG

### Các bảng DB liên quan:
- `orders` - Lưu thông tin đơn hàng chính
- `order_items` - Chi tiết sản phẩm trong đơn hàng
- `order_status_log` - Lịch sử thay đổi trạng thái
- `address` - Địa chỉ giao hàng
- `users` - Thông tin khách hàng
- `product` - Thông tin sản phẩm
- `size` - Kích thước
- `toppings` - Topping
- `ice_option` - Tùy chọn đá
- `voucher` - Mã giảm giá

---

## 🎯 GIAI ĐOẠN 1: THIẾT LẬP CẤU TRÚC BACKEND

### 1.1 Tạo Entity Models
**File:** `src/main/java/SpringBoot/demo/Entity/Order.java`

```
Cần tạo các entity:
- Order (id, userId, orderCode, totalAmount, status, paymentMethod, deliveryMethod, shippingFee, tax, createdAt, updatedAt)
- OrderItem (id, orderId, productId, sizeId, quantity, price, toppingIds)
- OrderStatusLog (id, orderId, status, note, createdAt)
```

**Yêu cầu:**
- [ ] Tạo `Order` entity với các trường cần thiết
- [ ] Tạo `OrderItem` entity (many-to-one với Order)
- [ ] Tạo `OrderStatusLog` entity (many-to-one với Order)
- [ ] Thêm JPA relationships (OneToMany, ManyToOne)
- [ ] Thêm validation annotations (@NotNull, @NotBlank, etc.)

---

### 1.2 Tạo DTO (Data Transfer Objects)
**File:** `src/main/java/SpringBoot/demo/DTO/`

```
Cần tạo các DTO:
- CreateOrderRequest (userId, items[], deliveryMethod, paymentMethod, shippingAddress)
- OrderItemRequest (productId, sizeId, quantity, toppingIds[])
- OrderResponse (id, orderCode, totalAmount, status, createdAt)
- OrderDetailResponse (kèm chi tiết items, address)
```

**Yêu cầu:**
- [ ] Tạo `CreateOrderRequest` DTO
- [ ] Tạo `OrderItemRequest` DTO
- [ ] Tạo `OrderResponse` DTO
- [ ] Tạo `OrderDetailResponse` DTO
- [ ] Thêm validation rules

---

### 1.3 Tạo Repository
**File:** `src/main/java/SpringBoot/demo/Repository/`

```
Cần tạo các repository:
- OrderRepository (extends JpaRepository<Order, Long>)
- OrderItemRepository
- OrderStatusLogRepository
```

**Yêu cầu:**
- [ ] Tạo `OrderRepository` với custom queries:
  - `findByUserId(Long userId)` - Lấy đơn hàng của user
  - `findByOrderCode(String orderCode)` - Tìm đơn hàng theo mã
  - `findByStatus(OrderStatus status)` - Lọc theo trạng thái
- [ ] Tạo `OrderItemRepository`
- [ ] Tạo `OrderStatusLogRepository`

---

## 🎯 GIAI ĐOẠN 2: LOGIC KINH DOANH (SERVICE LAYER)

### 2.1 Tạo OrderService
**File:** `src/main/java/SpringBoot/demo/Service/OrderService.java`

**Các phương thức cần triển khai:**

```java
// 1. Kiểm tra điều kiện trước khi tạo đơn hàng
validateOrderCreation(CreateOrderRequest request)
  - Kiểm tra user đã login chưa (JWT token)
  - Kiểm tra items không rỗng
  - Kiểm tra sản phẩm còn bán không
  - Kiểm tra giá sản phẩm có cập nhật không

// 2. Tính toán giá
calculateOrderTotal(List<OrderItem> items)
  - Tính tổng tiền sản phẩm
  - Thêm phí topping
  - Thêm thuế (VAT 10%)
  - Thêm phí ship (nếu delivery)
  - Trừ voucher (nếu có)

// 3. Tạo mã đơn hàng
generateOrderCode()
  - Format: ORDxxxx (ví dụ: ORD0001, ORD0002)

// 4. Tạo đơn hàng
createOrder(CreateOrderRequest request, Long userId)
  - Validate input
  - Tính toán tổng tiền
  - Tạo Order entity
  - Tạo OrderItems
  - Lưu vào DB
  - Trả về OrderResponse

// 5. Lấy thông tin đơn hàng
getOrderById(Long orderId, Long userId)
  - Kiểm tra quyền (user chỉ xem đơn của mình)
  - Trả về OrderDetailResponse

// 6. Lấy danh sách đơn hàng của user
getUserOrders(Long userId)
  - Lấy tất cả đơn hàng của user
  - Sắp xếp theo ngày tạo (mới nhất trước)

// 7. Cập nhật trạng thái đơn hàng
updateOrderStatus(Long orderId, OrderStatus newStatus, String note)
  - Cập nhật trạng thái
  - Lưu log thay đổi
```

**Yêu cầu:**
- [ ] Tạo `OrderService` class
- [ ] Implement các phương thức trên
- [ ] Thêm error handling và validation
- [ ] Thêm logging

---

### 2.2 Tạo Enum cho OrderStatus
**File:** `src/main/java/SpringBoot/demo/Enum/OrderStatus.java`

```java
enum OrderStatus {
    PENDING,           // Chờ xác nhận
    CONFIRMED,         // Đã xác nhận
    PREPARING,         // Đang chuẩn bị
    READY_FOR_PICKUP,  // Sẵn sàng lấy
    OUT_FOR_DELIVERY,  // Đang giao
    DELIVERED,         // Đã giao
    CANCELLED,         // Đã hủy
    REFUNDED           // Đã hoàn tiền
}
```

**Yêu cầu:**
- [ ] Tạo `OrderStatus` enum

---

### 2.3 Tạo Enum cho PaymentMethod
**File:** `src/main/java/SpringBoot/demo/Enum/PaymentMethod.java`

```java
enum PaymentMethod {
    COD,           // Tiền mặt
    BANK_TRANSFER, // Chuyển khoản
    VNPAY,         // VNPay
    MOMO           // Momo
}
```

**Yêu cầu:**
- [ ] Tạo `PaymentMethod` enum

---

### 2.4 Tạo Enum cho DeliveryMethod
**File:** `src/main/java/SpringBoot/demo/Enum/DeliveryMethod.java`

```java
enum DeliveryMethod {
    DELIVERY,  // Giao tận nơi
    PICKUP     // Lấy tại quán
}
```

**Yêu cầu:**
- [ ] Tạo `DeliveryMethod` enum

---

## 🎯 GIAI ĐOẠN 3: AUTHENTICATION & AUTHORIZATION

### 3.1 Cập nhật JwtAuthenticationFilter
**File:** `src/main/java/SpringBoot/demo/Security/JwtAuthenticationFilter.java`

**Yêu cầu:**
- [ ] Thêm `/orders/**` vào protected endpoints
- [ ] Đảm bảo tất cả endpoint đặt hàng yêu cầu JWT token

**Cập nhật:**
```java
private boolean isProtectedEndpoint(String uri) {
    return uri.startsWith("/auth/profile") || 
           uri.startsWith("/auth/logout") ||
           uri.startsWith("/auth/validate") ||
           uri.startsWith("/orders");  // ← THÊM DÒNG NÀY
}
```

---

### 3.2 Kiểm tra User từ Token
**Yêu cầu:**
- [ ] Trong OrderController, lấy userId từ JWT token
- [ ] Đảm bảo user chỉ có thể xem/sửa đơn hàng của chính mình

**Ví dụ:**
```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
String username = authentication.getName();
User user = userService.findByUsername(username);
Long userId = user.getId();
```

---

## 🎯 GIAI ĐOẠN 4: CONTROLLER & API ENDPOINTS

### 4.1 Tạo OrderController
**File:** `src/main/java/SpringBoot/demo/Controller/OrderController.java`

**Các endpoint cần tạo:**

```
1. POST /orders/create
   - Request: CreateOrderRequest
   - Response: OrderResponse
   - Mô tả: Tạo đơn hàng mới
   - Auth: Required (JWT)

2. GET /orders/{orderId}
   - Response: OrderDetailResponse
   - Mô tả: Lấy chi tiết đơn hàng
   - Auth: Required (JWT)

3. GET /orders
   - Response: List<OrderResponse>
   - Mô tả: Lấy danh sách đơn hàng của user
   - Auth: Required (JWT)

4. PUT /orders/{orderId}/cancel
   - Response: ApiResponse<String>
   - Mô tả: Hủy đơn hàng
   - Auth: Required (JWT)

5. GET /orders/{orderId}/status-log
   - Response: List<OrderStatusLog>
   - Mô tả: Lấy lịch sử thay đổi trạng thái
   - Auth: Required (JWT)
```

**Yêu cầu:**
- [ ] Tạo `OrderController` class
- [ ] Implement các endpoint trên
- [ ] Thêm @Secured hoặc @PreAuthorize để kiểm tra quyền
- [ ] Thêm Swagger documentation

---

## 🎯 GIAI ĐOẠN 5: VALIDATION & ERROR HANDLING

### 5.1 Validation Rules
**Yêu cầu:**
- [ ] Validate CreateOrderRequest:
  - userId không null
  - items không rỗng
  - deliveryMethod không null
  - paymentMethod không null
  - Nếu delivery: shippingAddress không null

- [ ] Validate OrderItemRequest:
  - productId không null
  - sizeId không null
  - quantity > 0
  - toppingIds có thể null (optional)

- [ ] Validate sản phẩm:
  - Sản phẩm tồn tại
  - Sản phẩm còn bán (is_available = true)
  - Size tồn tại
  - Topping tồn tại

---

### 5.2 Error Handling
**Yêu cầu:**
- [ ] Tạo custom exceptions:
  - `OrderNotFoundException`
  - `ProductNotAvailableException`
  - `InvalidOrderException`
  - `UnauthorizedOrderAccessException`

- [ ] Thêm @ExceptionHandler trong GlobalExceptionHandler

---

## 🎯 GIAI ĐOẠN 6: TESTING

### 6.1 Unit Tests
**File:** `src/test/java/SpringBoot/demo/Service/OrderServiceTest.java`

**Test cases:**
- [ ] Test tạo đơn hàng thành công
- [ ] Test tạo đơn hàng với sản phẩm không tồn tại
- [ ] Test tính toán giá đúng
- [ ] Test tạo mã đơn hàng unique
- [ ] Test lấy đơn hàng của user
- [ ] Test không thể xem đơn hàng của user khác

---

### 6.2 Integration Tests
**File:** `src/test/java/SpringBoot/demo/Controller/OrderControllerTest.java`

**Test cases:**
- [ ] Test POST /orders/create thành công
- [ ] Test POST /orders/create không có JWT token
- [ ] Test GET /orders/{orderId} thành công
- [ ] Test GET /orders/{orderId} không có quyền
- [ ] Test GET /orders lấy danh sách

---

## 📋 DANH SÁCH KIỂM TRA (CHECKLIST)

### Phase 1: Entity & DTO
- [ ] Order entity
- [ ] OrderItem entity
- [ ] OrderStatusLog entity
- [ ] CreateOrderRequest DTO
- [ ] OrderItemRequest DTO
- [ ] OrderResponse DTO
- [ ] OrderDetailResponse DTO

### Phase 2: Repository & Service
- [ ] OrderRepository
- [ ] OrderItemRepository
- [ ] OrderStatusLogRepository
- [ ] OrderService (7 phương thức)
- [ ] OrderStatus enum
- [ ] PaymentMethod enum
- [ ] DeliveryMethod enum

### Phase 3: Security
- [ ] Cập nhật JwtAuthenticationFilter
- [ ] Thêm protected endpoints cho /orders/**
- [ ] Implement user authorization check

### Phase 4: Controller & API
- [ ] OrderController
- [ ] 5 endpoints (create, get, list, cancel, status-log)
- [ ] Swagger documentation
- [ ] Error handling

### Phase 5: Testing
- [ ] Unit tests cho OrderService
- [ ] Integration tests cho OrderController
- [ ] Manual testing với Postman/Swagger

---

## 🔐 SECURITY CHECKLIST

- [ ] Tất cả endpoint /orders yêu cầu JWT token
- [ ] User chỉ có thể xem/sửa đơn hàng của chính mình
- [ ] Validate user từ token trước khi xử lý
- [ ] Không cho phép user thay đổi userId trong request
- [ ] Kiểm tra quyền trước khi cập nhật trạng thái đơn hàng

---

## 💾 DATABASE SCHEMA (Nếu cần tạo thêm)

```sql
-- Nếu bảng orders chưa có các trường sau, hãy thêm:
ALTER TABLE orders ADD COLUMN IF NOT EXISTS tax DECIMAL(10, 2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_fee DECIMAL(10, 2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_method VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_code VARCHAR(20) UNIQUE;

-- Tạo index để tăng tốc độ query
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
```

---

## 📝 GHI CHÚ QUAN TRỌNG

1. **Mã đơn hàng:** Sử dụng format `ORDxxxx` với số tự động tăng
2. **Tính toán giá:**
   - Tổng = (Giá SP + Giá Topping) × Số lượng + Thuế (10%) + Phí Ship
3. **Trạng thái đơn hàng:** Luôn ghi log khi thay đổi
4. **Giao hàng:** Nếu DELIVERY, bắt buộc có địa chỉ giao hàng
5. **Thanh toán:** Hỗ trợ 4 phương thức (COD, Bank, VNPay, Momo)
6. **Validation:** Kiểm tra sản phẩm còn bán trước khi tạo đơn

---

## 🚀 NEXT STEPS

1. Cung cấp schema của bảng `orders`, `order_items`, `order_status_log` (nếu chưa có)
2. Xác nhận các trường cần thiết trong Order entity
3. Bắt đầu từ Phase 1: Tạo Entity & DTO
4. Sau đó là Phase 2: Repository & Service
5. Tiếp theo Phase 3: Security
6. Rồi Phase 4: Controller
7. Cuối cùng Phase 5: Testing

---

**Cập nhật lần cuối:** 2025-01-20
**Trạng thái:** Chờ xác nhận schema DB
