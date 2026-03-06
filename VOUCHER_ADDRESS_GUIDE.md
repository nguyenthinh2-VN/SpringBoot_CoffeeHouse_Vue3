# 📋 HƯỚNG DẪN CHỨC NĂNG VOUCHER & ADDRESS

## 1️⃣ CHỨC NĂNG ADDRESS (ĐỊA CHỈ GIAO HÀNG)

### 📍 Nghiệp vụ

**Địa chỉ giao hàng** là nơi khách muốn nhận hàng.

#### Hai trường hợp:

| Phương thức | Địa chỉ | Phí ship | Ví dụ |
|-----------|--------|---------|------|
| **PICKUP** (Lấy tại quán) | ❌ Không cần | 0 VND | Khách tự đến quán lấy |
| **DELIVERY** (Giao tận nơi) | ✅ Bắt buộc | 30,000 VND | Khách ở nhà, shipper giao |

### 🔄 Quy trình

```
Khách chọn PICKUP
  ↓
Không cần nhập địa chỉ
  ↓
Phí ship = 0
  ↓
Tổng tiền = SP + Tax

---

Khách chọn DELIVERY
  ↓
Bắt buộc nhập địa chỉ + số điện thoại
  ↓
Phí ship = 30,000 VND
  ↓
Tổng tiền = SP + Tax + 30,000
```

### 💾 Lưu trữ

Địa chỉ được lưu trong bảng `orders`:
- `shipping_address` - Địa chỉ giao hàng
- `phone_number` - Số điện thoại liên hệ

---

## 2️⃣ CHỨC NĂNG VOUCHER (MÃ GIẢM GIÁ)

### 🎟️ Nghiệp vụ

**Voucher** là mã giảm giá mà khách có thể sử dụng để giảm giá đơn hàng.

#### Ví dụ voucher:

| Mã | Loại | Giảm | Điều kiện | Trạng thái |
|----|------|------|----------|-----------|
| FREESHIP | Phí ship | 30,000 VND | Không | Hoạt động |
| SAVE20 | Phần trăm | 20% | Tối thiểu 100,000 | Hoạt động |
| SAVE50K | Cố định | 50,000 VND | Tối thiểu 200,000 | Hết hạn |

### 🔄 Quy trình

```
Khách nhập mã voucher: "FREESHIP"
  ↓
Hệ thống kiểm tra:
  - Voucher tồn tại không?
  - Voucher còn hoạt động không?
  - Đã hết hạn chưa?
  - Tổng tiền có đạt điều kiện không?
  ↓
Nếu hợp lệ:
  - Tính discount
  - Cập nhật tổng tiền
  - Lưu voucher code vào đơn hàng
  ↓
Nếu không hợp lệ:
  - Trả về lỗi
  - Không áp dụng giảm giá
```

### 💾 Lưu trữ

Tạo bảng `vouchers`:

```sql
CREATE TABLE vouchers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_type ENUM('FIXED', 'PERCENT') NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2),
    max_discount DECIMAL(10, 2),
    is_active BOOLEAN DEFAULT TRUE,
    start_date DATETIME,
    end_date DATETIME,
    usage_limit INT,
    usage_count INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
);
```

### 📊 Ví dụ dữ liệu

```sql
-- Giảm phí ship 30,000
INSERT INTO vouchers VALUES 
(1, 'FREESHIP', 'FIXED', 30000, NULL, 30000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, NOW(), NOW());

-- Giảm 20% (tối đa 100,000)
INSERT INTO vouchers VALUES 
(2, 'SAVE20', 'PERCENT', 20, 100000, 100000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, NOW(), NOW());

-- Giảm cố định 50,000 (tối thiểu 200,000)
INSERT INTO vouchers VALUES 
(3, 'SAVE50K', 'FIXED', 50000, 200000, 50000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, NOW(), NOW());
```

---

## 3️⃣ TÍNH TOÁN TỔNG TIỀN

### 📐 Công thức

```
Subtotal = Σ(Giá SP + Giá Size + Giá Topping) × Số lượng

Tax = Subtotal × 10%

ShippingFee = 0 (PICKUP) hoặc 30,000 (DELIVERY)

Discount = 0 (nếu không có voucher)
         = Cố định (nếu FIXED)
         = Subtotal × Phần trăm (nếu PERCENT)

TotalAmount = Subtotal + Tax + ShippingFee - Discount
```

### 🧮 Ví dụ tính toán

#### Trường hợp 1: PICKUP, không voucher

```
Sản phẩm: Cà phê 30,000 + Size L 5,000 + Topping 10,000 = 45,000
Số lượng: 1
Subtotal = 45,000

Tax = 45,000 × 10% = 4,500
ShippingFee = 0 (PICKUP)
Discount = 0

TotalAmount = 45,000 + 4,500 + 0 - 0 = 49,500 VND
```

#### Trường hợp 2: DELIVERY, voucher FREESHIP

```
Sản phẩm: Cà phê 30,000 + Size L 5,000 + Topping 10,000 = 45,000
Số lượng: 1
Subtotal = 45,000

Tax = 45,000 × 10% = 4,500
ShippingFee = 30,000 (DELIVERY)
Voucher = "FREESHIP" → Discount = 30,000

TotalAmount = 45,000 + 4,500 + 30,000 - 30,000 = 49,500 VND
```

#### Trường hợp 3: DELIVERY, voucher SAVE20 (20% giảm)

```
Sản phẩm: Cà phê 30,000 + Size L 5,000 + Topping 10,000 = 45,000
Số lượng: 2
Subtotal = 90,000

Tax = 90,000 × 10% = 9,000
ShippingFee = 30,000 (DELIVERY)
Voucher = "SAVE20" → Discount = 90,000 × 20% = 18,000 (tối đa 100,000)

TotalAmount = 90,000 + 9,000 + 30,000 - 18,000 = 111,000 VND
```

---

## 4️⃣ VALIDATION RULES

### ✅ Kiểm tra Address

```
Nếu deliveryMethod = DELIVERY:
  - shippingAddress không được null
  - shippingAddress không được rỗng
  - phoneNumber không được null
  - phoneNumber không được rỗng
  
Nếu deliveryMethod = PICKUP:
  - shippingAddress có thể null
  - phoneNumber có thể null (hoặc để trống)
```

### ✅ Kiểm tra Voucher

```
Nếu voucherCode được cung cấp:
  1. Kiểm tra voucher tồn tại
  2. Kiểm tra is_active = TRUE
  3. Kiểm tra start_date <= NOW() <= end_date
  4. Kiểm tra subtotal >= min_order_amount (nếu có)
  5. Kiểm tra usage_count < usage_limit (nếu có)
  
Nếu tất cả hợp lệ:
  - Áp dụng discount
  - Tăng usage_count
  
Nếu không hợp lệ:
  - Trả về lỗi
  - Không áp dụng discount
```

---

## 5️⃣ API REQUEST EXAMPLES

### ✅ Ví dụ 1: PICKUP, không voucher

```json
POST /orders/create
{
  "items": [
    {
      "productId": 1,
      "sizeId": 1,
      "quantity": 1,
      "toppingIds": [1, 2],
      "iceOptionId": 1
    }
  ],
  "deliveryMethod": "PICKUP",
  "paymentMethod": "COD"
}
```

**Response:**
```json
{
  "totalAmount": 49500,
  "tax": 4500,
  "shippingFee": 0,
  "discount": 0
}
```

---

### ✅ Ví dụ 2: DELIVERY, với voucher FREESHIP

```json
POST /orders/create
{
  "items": [
    {
      "productId": 1,
      "sizeId": 1,
      "quantity": 1,
      "toppingIds": [1, 2]
    }
  ],
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "shippingAddress": "123 Nguyễn Huệ, Quận 1, TP.HCM",
  "phoneNumber": "0123456789",
  "voucherCode": "FREESHIP"
}
```

**Response:**
```json
{
  "totalAmount": 49500,
  "tax": 4500,
  "shippingFee": 30000,
  "discount": 30000,
  "voucherCode": "FREESHIP"
}
```

---

### ❌ Ví dụ 3: DELIVERY, không có địa chỉ (LỖI)

```json
POST /orders/create
{
  "items": [...],
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "phoneNumber": "0123456789"
}
```

**Response:**
```json
{
  "success": false,
  "message": "Địa chỉ giao hàng không được để trống"
}
```

---

### ❌ Ví dụ 4: Voucher không hợp lệ (LỖI)

```json
POST /orders/create
{
  "items": [...],
  "deliveryMethod": "PICKUP",
  "paymentMethod": "COD",
  "voucherCode": "INVALID_CODE"
}
```

**Response:**
```json
{
  "success": false,
  "message": "Mã voucher không hợp lệ hoặc đã hết hạn"
}
```

---

## 6️⃣ FLOW DIAGRAM

```
┌─────────────────────────────────────┐
│   Khách gửi request tạo đơn hàng    │
└──────────────┬──────────────────────┘
               │
               ▼
        ┌──────────────┐
        │ Validate     │
        │ deliveryMethod
        └──────┬───────┘
               │
        ┌──────┴──────┐
        │             │
    PICKUP        DELIVERY
        │             │
        │        ┌────▼────────────┐
        │        │ Kiểm tra        │
        │        │ address & phone  │
        │        └────┬────────────┘
        │             │
        │        ┌────▼──────────┐
        │        │ Hợp lệ?       │
        │        └────┬──────┬───┘
        │             │      │
        │           YES     NO
        │             │      │
        │             │   ❌ Error
        │             │
        └─────┬───────┘
              │
              ▼
        ┌──────────────────┐
        │ Kiểm tra voucher │
        │ (nếu có)         │
        └────┬─────────────┘
             │
        ┌────▼──────────┐
        │ Hợp lệ?       │
        └────┬──────┬───┘
             │      │
           YES     NO
             │      │
             │   ❌ Error
             │
             ▼
        ┌──────────────────┐
        │ Tính toán giá    │
        │ (SP+Tax+Ship-Disc)
        └────┬─────────────┘
             │
             ▼
        ┌──────────────────┐
        │ Lưu đơn hàng     │
        └────┬─────────────┘
             │
             ▼
        ✅ Thành công
```

---

## 📝 SUMMARY

| Tính năng | Mô tả | Bắt buộc |
|----------|-------|---------|
| **Address** | Địa chỉ giao hàng | Chỉ khi DELIVERY |
| **Voucher** | Mã giảm giá | Tùy chọn |
| **Tax** | Thuế 10% | Luôn luôn |
| **ShippingFee** | Phí giao hàng | 0 (PICKUP) hoặc 30k (DELIVERY) |
| **Discount** | Giảm giá từ voucher | Tùy chọn |

---

**Sẵn sàng code chưa?** 🚀
