# ✅ CHỨC NĂNG ĐƠN HÀNG - HOÀN THÀNH PHASE 1-5

## 📋 TỔNG QUAN

Đã hoàn thành triển khai chức năng đặt hàng (Order Management) cho hệ thống CoffeeHouse. Bao gồm:
- Entity Models
- DTO (Data Transfer Objects)
- Repository
- Service Layer
- Controller & API Endpoints
- Security Integration

---

## ✅ PHASE 1: ENUM (Hoàn thành)

### Các Enum được tạo:

1. **OrderStatus.java** - Trạng thái đơn hàng
   - PENDING (Chờ xác nhận)
   - CONFIRMED (Đã xác nhận)
   - PREPARING (Đang chuẩn bị)
   - READY_FOR_PICKUP (Sẵn sàng lấy)
   - OUT_FOR_DELIVERY (Đang giao)
   - DELIVERED (Đã giao)
   - CANCELLED (Đã hủy)
   - REFUNDED (Đã hoàn tiền)

2. **PaymentMethod.java** - Phương thức thanh toán
   - COD (Tiền mặt)
   - BANK_TRANSFER (Chuyển khoản)
   - VNPAY (VNPay)
   - MOMO (Momo)

3. **DeliveryMethod.java** - Phương thức giao hàng
   - DELIVERY (Giao tận nơi)
   - PICKUP (Lấy tại quán)

---

## ✅ PHASE 2: ENTITY MODELS (Hoàn thành)

### 1. Order.java
**Bảng:** `orders`

**Các trường:**
- `id` (Long) - Primary Key
- `userId` (Long) - Khách hàng
- `orderCode` (String) - Mã đơn hàng (ORDxxxx)
- `totalAmount` (BigDecimal) - Tổng tiền
- `tax` (BigDecimal) - Thuế (10%)
- `shippingFee` (BigDecimal) - Phí giao hàng
- `status` (OrderStatus) - Trạng thái
- `paymentMethod` (PaymentMethod) - Phương thức thanh toán
- `deliveryMethod` (DeliveryMethod) - Phương thức giao hàng
- `shippingAddress` (String) - Địa chỉ giao hàng
- `phoneNumber` (String) - Số điện thoại
- `notes` (String) - Ghi chú
- `createdAt` (LocalDateTime) - Ngày tạo
- `updatedAt` (LocalDateTime) - Ngày cập nhật

**Relationships:**
- OneToMany → OrderItem
- OneToMany → OrderStatusLog

### 2. OrderItem.java
**Bảng:** `order_items`

**Các trường:**
- `id` (Long) - Primary Key
- `orderId` (Long) - FK → Order
- `productId` (Integer) - Sản phẩm
- `productName` (String) - Tên sản phẩm
- `sizeId` (Integer) - Kích thước
- `sizeName` (String) - Tên kích thước
- `quantity` (Integer) - Số lượng
- `price` (BigDecimal) - Giá sản phẩm
- `toppingIds` (String) - JSON array [1,2,3]
- `toppingNames` (String) - Tên topping
- `toppingPrice` (BigDecimal) - Giá topping
- `iceOptionId` (Integer) - Tùy chọn đá
- `iceOptionName` (String) - Tên tùy chọn đá
- `notes` (String) - Ghi chú

**Relationships:**
- ManyToOne → Order

### 3. OrderStatusLog.java
**Bảng:** `order_status_log`

**Các trường:**
- `id` (Long) - Primary Key
- `orderId` (Long) - FK → Order
- `status` (OrderStatus) - Trạng thái
- `notes` (String) - Ghi chú
- `createdAt` (LocalDateTime) - Ngày tạo

**Relationships:**
- ManyToOne → Order

---

## ✅ PHASE 3: DTO (Hoàn thành)

### Request DTOs:
1. **CreateOrderRequest** - Tạo đơn hàng
   - items (List<OrderItemRequest>) - Danh sách sản phẩm
   - deliveryMethod (DeliveryMethod) - Phương thức giao
   - paymentMethod (PaymentMethod) - Phương thức thanh toán
   - shippingAddress (String) - Địa chỉ giao
   - phoneNumber (String) - Số điện thoại
   - notes (String) - Ghi chú
   - voucherCode (String) - Mã giảm giá

2. **OrderItemRequest** - Chi tiết sản phẩm
   - productId (Integer)
   - sizeId (Integer)
   - quantity (Integer)
   - toppingIds (List<Integer>)
   - iceOptionId (Integer)
   - notes (String)

### Response DTOs:
1. **OrderResponse** - Thông tin đơn hàng
2. **OrderDetailResponse** - Chi tiết đơn hàng (kèm items + status logs)
3. **OrderItemResponse** - Chi tiết sản phẩm trong đơn
4. **OrderStatusLogResponse** - Lịch sử thay đổi trạng thái

---

## ✅ PHASE 4: REPOSITORY (Hoàn thành)

### 1. OrderRepository
**Các method:**
- `findByOrderCode(String)` - Tìm theo mã đơn
- `findByUserId(Long)` - Lấy đơn của user
- `findByUserIdAndStatus(Long, OrderStatus)` - Lọc theo trạng thái
- `findByStatus(OrderStatus)` - Lấy theo trạng thái
- `findUserOrdersLatest(Long)` - Lấy đơn mới nhất của user
- `findOrdersByDateRange(LocalDateTime, LocalDateTime)` - Lấy theo khoảng thời gian
- `existsByOrderCode(String)` - Kiểm tra mã đơn tồn tại
- `countByUserId(Long)` - Đếm đơn của user
- `countByStatus(OrderStatus)` - Đếm theo trạng thái

### 2. OrderItemRepository
**Các method:**
- `findByOrderId(Long)` - Lấy items của đơn
- `findByOrderAndProduct(Long, Integer)` - Lấy item theo order + product
- `countByOrderId(Long)` - Đếm items

### 3. OrderStatusLogRepository
**Các method:**
- `findByOrderIdOrderByCreatedAtDesc(Long)` - Lấy lịch sử
- `findByStatus(OrderStatus)` - Lấy log theo trạng thái
- `countByOrderId(Long)` - Đếm lần thay đổi

---

## ✅ PHASE 5: SERVICE LAYER (Hoàn thành)

### OrderService.java

**Các phương thức:**

1. **createOrder(CreateOrderRequest, Long userId)** ✅
   - Validate input
   - Kiểm tra sản phẩm tồn tại
   - Tính toán giá (SP + Size + Topping + Tax + Ship)
   - Tạo mã đơn hàng (ORDxxxx)
   - Lưu Order + OrderItems + StatusLog
   - Return OrderResponse

2. **getOrderDetail(Long orderId, Long userId)** ✅
   - Kiểm tra quyền (user chỉ xem đơn của mình)
   - Trả về OrderDetailResponse (kèm items + status logs)

3. **getUserOrders(Long userId)** ✅
   - Lấy tất cả đơn của user
   - Sắp xếp theo ngày tạo (mới nhất trước)
   - Return List<OrderResponse>

4. **cancelOrder(Long orderId, Long userId)** ✅
   - Kiểm tra quyền
   - Kiểm tra trạng thái (chỉ hủy PENDING/CONFIRMED)
   - Cập nhật status → CANCELLED
   - Tạo log

5. **getOrderStatusLog(Long orderId, Long userId)** ✅
   - Kiểm tra quyền
   - Return List<OrderStatusLogResponse>

6. **updateOrderStatus(Long orderId, OrderStatus, String notes)** ✅
   - Cập nhật trạng thái (Admin)
   - Tạo log

7. **generateOrderCode()** ✅
   - Tạo mã duy nhất: ORDxxxx

**Tính toán giá:**
```
Subtotal = Σ(Giá SP + Giá Size + Giá Topping) × Số lượng
Tax = Subtotal × 10%
ShippingFee = 30,000 VND (nếu DELIVERY) hoặc 0 (nếu PICKUP)
Total = Subtotal + Tax + ShippingFee
```

---

## ✅ PHASE 6: CONTROLLER & API (Hoàn thành)

### OrderController.java

**5 Endpoints:**

1. **POST /orders/create** ✅
   - Tạo đơn hàng mới
   - Auth: Required (JWT)
   - Status: 201 (Created)

2. **GET /orders/{orderId}** ✅
   - Lấy chi tiết đơn hàng
   - Auth: Required (JWT)
   - Status: 200 (OK)

3. **GET /orders** ✅
   - Lấy danh sách đơn của user
   - Auth: Required (JWT)
   - Status: 200 (OK)

4. **PUT /orders/{orderId}/cancel** ✅
   - Hủy đơn hàng
   - Auth: Required (JWT)
   - Status: 200 (OK)

5. **GET /orders/{orderId}/status-log** ✅
   - Lấy lịch sử thay đổi trạng thái
   - Auth: Required (JWT)
   - Status: 200 (OK)

**Security:**
- Tất cả endpoint yêu cầu JWT token
- User chỉ xem được đơn của chính mình
- Lấy userId từ token → kiểm tra quyền

---

## ✅ PHASE 7: SECURITY (Hoàn thành)

### Cập nhật JwtAuthenticationFilter.java
- Thêm `/orders/**` vào protected endpoints
- Tất cả request đến /orders phải có JWT token hợp lệ

### Authorization Check
- Mỗi endpoint kiểm tra userId từ token
- User chỉ có thể xem/sửa đơn của chính mình
- Admin có thể cập nhật trạng thái (sẽ implement sau)

---

## 📊 DATABASE SCHEMA

Nếu chưa có, hãy chạy SQL sau:

```sql
-- Bảng orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_code VARCHAR(20) UNIQUE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2),
    shipping_fee DECIMAL(10, 2),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50) NOT NULL,
    delivery_method VARCHAR(50) NOT NULL,
    shipping_address VARCHAR(255),
    phone_number VARCHAR(20),
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bảng order_items
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(255),
    size_id INT NOT NULL,
    size_name VARCHAR(100),
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    topping_ids TEXT,
    topping_names VARCHAR(255),
    topping_price DECIMAL(10, 2),
    ice_option_id INT,
    ice_option_name VARCHAR(100),
    notes TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Bảng order_status_log
CREATE TABLE IF NOT EXISTS order_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Tạo index
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_status_log_order_id ON order_status_log(order_id);
```

---

## 🔧 CẤU HÌNH DEPENDENCIES

Đảm bảo `pom.xml` có:
```xml
<!-- ModelMapper -->
<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
    <version>3.1.1</version>
</dependency>

<!-- Jackson (JSON) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
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

---

## 📝 DANH SÁCH FILES ĐÃ TẠO

### Enum (3 files)
- ✅ `src/main/java/SpringBoot/demo/Enum/OrderStatus.java`
- ✅ `src/main/java/SpringBoot/demo/Enum/PaymentMethod.java`
- ✅ `src/main/java/SpringBoot/demo/Enum/DeliveryMethod.java`

### Model (3 files)
- ✅ `src/main/java/SpringBoot/demo/Model/Order.java`
- ✅ `src/main/java/SpringBoot/demo/Model/OrderItem.java`
- ✅ `src/main/java/SpringBoot/demo/Model/OrderStatusLog.java`

### DTO (6 files)
- ✅ `src/main/java/SpringBoot/demo/DTO/CreateOrderRequest.java`
- ✅ `src/main/java/SpringBoot/demo/DTO/OrderItemRequest.java`
- ✅ `src/main/java/SpringBoot/demo/DTO/OrderResponse.java`
- ✅ `src/main/java/SpringBoot/demo/DTO/OrderDetailResponse.java`
- ✅ `src/main/java/SpringBoot/demo/DTO/OrderItemResponse.java`
- ✅ `src/main/java/SpringBoot/demo/DTO/OrderStatusLogResponse.java`

### Repository (3 files)
- ✅ `src/main/java/SpringBoot/demo/Repository/OrderRepository.java`
- ✅ `src/main/java/SpringBoot/demo/Repository/OrderItemRepository.java`
- ✅ `src/main/java/SpringBoot/demo/Repository/OrderStatusLogRepository.java`

### Service (1 file)
- ✅ `src/main/java/SpringBoot/demo/Service/OrderService.java`

### Controller (1 file)
- ✅ `src/main/java/SpringBoot/demo/Controller/OrderController.java`

### Updated Files (1 file)
- ✅ `src/main/java/SpringBoot/demo/Security/JwtAuthenticationFilter.java` (thêm /orders)

---

## 🚀 NEXT STEPS

### Cần làm tiếp:

1. **Kiểm tra DB Schema**
   - [ ] Chạy SQL tạo bảng (nếu chưa có)
   - [ ] Verify các trường trong DB

2. **Test API**
   - [ ] Test POST /orders/create
   - [ ] Test GET /orders
   - [ ] Test GET /orders/{id}
   - [ ] Test PUT /orders/{id}/cancel
   - [ ] Test GET /orders/{id}/status-log

3. **Thêm Admin Endpoints** (tùy chọn)
   - [ ] GET /admin/orders - Lấy tất cả đơn
   - [ ] PUT /admin/orders/{id}/status - Cập nhật trạng thái
   - [ ] GET /admin/orders/stats - Thống kê

4. **Thêm Voucher/Discount** (tùy chọn)
   - [ ] Validate voucher code
   - [ ] Tính toán discount
   - [ ] Cập nhật total

5. **Thêm Payment Integration** (tùy chọn)
   - [ ] VNPay integration
   - [ ] Momo integration

6. **Unit & Integration Tests**
   - [ ] Test OrderService
   - [ ] Test OrderController
   - [ ] Test authorization

---

## 📞 SUPPORT

Nếu có lỗi khi compile hoặc run:

1. **Import không tìm thấy:**
   - Kiểm tra package name
   - Rebuild project (Ctrl + Shift + F9)

2. **Database error:**
   - Chạy SQL schema
   - Kiểm tra connection string

3. **JWT error:**
   - Đảm bảo token hợp lệ
   - Kiểm tra JwtAuthenticationFilter

---

**Cập nhật lần cuối:** 2025-01-20
**Status:** ✅ HOÀN THÀNH PHASE 1-7
**Sẵn sàng:** Test API
