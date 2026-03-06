# ✅ CHỨC NĂNG TÍNH PHÍ SHIP THEO KHU VỰC - HOÀN THÀNH

## 📋 TÓM TẮT

Đã hoàn thành triển khai chức năng tính phí giao hàng **động** theo khu vực (Quận/Huyện + Thành phố).

---

## 🎯 FILES ĐÃ TẠO/CẬP NHẬT

### **1. Entity Models**
- ✅ `ShippingZone.java` - Entity mới cho khu vực giao hàng

### **2. Repository**
- ✅ `ShippingZoneRepository.java` - Repository với custom queries

### **3. Service**
- ✅ `OrderService.java` - Cập nhật:
  - Thêm `ShippingZoneRepository` injection
  - Cập nhật `createOrder()` - validate district, city
  - Thêm method `getShippingFee()` - lấy phí ship từ khu vực
  - Thêm inner class `ShippingZoneResult` - lưu kết quả

### **4. DTO**
- ✅ `CreateOrderRequest.java` - Cập nhật: thêm `district`, `city`

### **5. Model**
- ✅ `Order.java` - Cập nhật: thêm `district`, `city`

### **6. Database**
- ✅ `CREATE_SHIPPING_ZONES_TABLE.sql` - SQL schema + dữ liệu mẫu

---

## 🔄 FLOW TÍNH PHÍ SHIP

```
Khách chọn DELIVERY
  ↓
Nhập district (Quận/Huyện) + city (Thành phố)
  ↓
Hệ thống tìm trong bảng shipping_zones
  ↓
Nếu tìm thấy:
  - Lấy phí ship từ bảng
  - Áp dụng vào tổng tiền
  ↓
Nếu không tìm thấy:
  - Trả về error: "Khu vực không được hỗ trợ"
```

---

## 💰 TÍNH TOÁN GIÁ (CẬP NHẬT)

### **Công thức**

```
Subtotal = Σ(Giá SP + Giá Size + Giá Topping) × Số lượng
Tax = Subtotal × 10%
ShippingFee = 0 (PICKUP) hoặc lấy từ shipping_zones (DELIVERY)
Discount = 0 (không voucher) hoặc tính từ voucher
TotalAmount = Subtotal + Tax + ShippingFee - Discount
```

### **Ví dụ 1: PICKUP**

```
Sản phẩm: 45,000 × 1
Subtotal = 45,000
Tax = 4,500
ShippingFee = 0
Discount = 0
TotalAmount = 49,500 VND
```

### **Ví dụ 2: DELIVERY - Quận 1 (15,000)**

```
Sản phẩm: 45,000 × 1
Subtotal = 45,000
Tax = 4,500
ShippingFee = 15,000 (Quận 1)
Discount = 0
TotalAmount = 64,500 VND
```

### **Ví dụ 3: DELIVERY - Huyện Cần Giờ (35,000)**

```
Sản phẩm: 45,000 × 1
Subtotal = 45,000
Tax = 4,500
ShippingFee = 35,000 (Huyện Cần Giờ)
Discount = 0
TotalAmount = 84,500 VND
```

---

## 🗄️ DATABASE SCHEMA

### **Bảng shipping_zones**

```sql
CREATE TABLE shipping_zones (
    id INT PRIMARY KEY AUTO_INCREMENT,
    zone_name VARCHAR(100) NOT NULL,      -- Tên khu vực
    district VARCHAR(100) NOT NULL,       -- Quận/Huyện
    city VARCHAR(100) NOT NULL,           -- Thành phố
    shipping_fee DECIMAL(10, 2) NOT NULL, -- Phí giao hàng
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    UNIQUE KEY uk_district_city (district, city)
);
```

### **Cập nhật bảng orders**

```sql
ALTER TABLE orders ADD COLUMN district VARCHAR(100);
ALTER TABLE orders ADD COLUMN city VARCHAR(100);
```

---

## 📊 PHÂN LOẠI PHÍ SHIP

### **TP.HCM**

| Loại | Khu vực | Phí ship |
|------|--------|---------|
| **Quận trung tâm** | Q1, Q3, Q4, Q5, Q6, Q10, Q11 | 15,000 - 18,000 |
| **Quận ngoài** | Q2, Q7, Q8, Q9, Q12, Tân Bình, Tân Phú, Phú Nhuận, Bình Thạnh, Gò Vấp | 20,000 - 25,000 |
| **Huyện** | Bình Chánh, Cần Giờ, Củ Chi, Hóc Môn, Nhà Bè | 28,000 - 35,000 |
| **Thành phố Thủ Đức** | Thành phố Thủ Đức | 25,000 |

---

## 🚀 SETUP STEPS

### **Bước 1: Chạy SQL schema**

```sql
-- Chạy file CREATE_SHIPPING_ZONES_TABLE.sql
-- Hoặc chạy từng câu lệnh
```

### **Bước 2: Build project**

```bash
Ctrl + Shift + F9  # Rebuild project
```

### **Bước 3: Test API**

```
POST /orders/create
Authorization: Bearer <token>

{
  "items": [{...}],
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "shippingAddress": "123 Nguyễn Huệ",
  "phoneNumber": "0123456789",
  "district": "Quận 1",
  "city": "TP.HCM"
}
```

---

## ✅ TEST CASES

### **Test 1: DELIVERY - Quận 1 (15,000)**

```json
Request:
{
  "items": [{...}],  // Subtotal = 45,000
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "shippingAddress": "123 Nguyễn Huệ",
  "phoneNumber": "0123456789",
  "district": "Quận 1",
  "city": "TP.HCM"
}

Response:
{
  "totalAmount": 64500,
  "tax": 4500,
  "shippingFee": 15000,
  "discount": 0,
  "district": "Quận 1",
  "city": "TP.HCM"
}
```

### **Test 2: DELIVERY - Huyện Cần Giờ (35,000)**

```json
Request:
{
  "items": [{...}],  // Subtotal = 45,000
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "shippingAddress": "Cần Giờ",
  "phoneNumber": "0123456789",
  "district": "Huyện Cần Giờ",
  "city": "TP.HCM"
}

Response:
{
  "totalAmount": 84500,
  "tax": 4500,
  "shippingFee": 35000,
  "discount": 0,
  "district": "Huyện Cần Giờ",
  "city": "TP.HCM"
}
```

### **Test 3: DELIVERY - Khu vực không hỗ trợ (ERROR)**

```json
Request:
{
  "items": [{...}],
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "shippingAddress": "...",
  "phoneNumber": "0123456789",
  "district": "Quận 99",
  "city": "TP.HCM"
}

Response:
{
  "success": false,
  "message": "Khu vực giao hàng không được hỗ trợ: Quận 99, TP.HCM"
}
```

### **Test 4: DELIVERY - Thiếu district (ERROR)**

```json
Request:
{
  "items": [{...}],
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "shippingAddress": "...",
  "phoneNumber": "0123456789",
  "city": "TP.HCM"
}

Response:
{
  "success": false,
  "message": "Quận/Huyện không được để trống"
}
```

---

## 📝 VALIDATION RULES

### **Address Validation (DELIVERY)**

- ✓ shippingAddress không null, không rỗng
- ✓ phoneNumber không null, không rỗng
- ✓ district không null, không rỗng **← NEW**
- ✓ city không null, không rỗng **← NEW**
- ✓ district + city phải tồn tại trong bảng shipping_zones

### **Address Validation (PICKUP)**

- ✓ Không cần district, city
- ✓ Phí ship = 0

---

## 🔍 QUERY EXAMPLES

### **Tìm phí ship theo district + city**

```sql
SELECT shipping_fee FROM shipping_zones 
WHERE district = 'Quận 1' AND city = 'TP.HCM' AND is_active = TRUE;
```

### **Lấy tất cả khu vực hoạt động**

```sql
SELECT * FROM shipping_zones WHERE is_active = TRUE ORDER BY city, district;
```

### **Cập nhật phí ship**

```sql
UPDATE shipping_zones SET shipping_fee = 18000 
WHERE district = 'Quận 1' AND city = 'TP.HCM';
```

### **Vô hiệu hóa khu vực**

```sql
UPDATE shipping_zones SET is_active = FALSE 
WHERE district = 'Huyện Cần Giờ' AND city = 'TP.HCM';
```

---

## 🌍 MỞ RỘNG (FUTURE)

Có thể thêm:
1. **Nhiều thành phố** - Hà Nội, Đà Nẵng, etc.
2. **Phí ship động** - Theo trọng lượng, khoảng cách
3. **Thời gian giao hàng** - Ước tính ngày giao
4. **Shipper management** - Quản lý shipper theo khu vực

---

## 📊 LOGIC DIAGRAM

```
┌─────────────────────────────┐
│ Khách gửi request tạo đơn   │
└──────────────┬──────────────┘
               │
               ▼
        ┌──────────────────┐
        │ Validate items   │
        └────┬─────────────┘
             │
             ▼
        ┌──────────────────┐
        │ Validate address │
        └────┬─────────────┘
             │
        ┌────┴──────┐
        │           │
    PICKUP      DELIVERY
        │           │
        │      ┌────▼──────────┐
        │      │ Validate      │
        │      │ district, city│
        │      └────┬──────────┘
        │           │
        │      ┌────▼──────────┐
        │      │ Tìm phí ship  │
        │      │ từ DB         │
        │      └────┬──────────┘
        │           │
        │      ┌────▼──────────┐
        │      │ Tìm thấy?     │
        │      └────┬──────┬───┘
        │           │      │
        │         YES     NO
        │           │      │
        │           │   ❌ Error
        │           │
        └─────┬─────┘
              │
              ▼
        ┌──────────────────┐
        │ Tính subtotal    │
        └────┬─────────────┘
             │
             ▼
        ┌──────────────────┐
        │ Validate voucher │
        │ (nếu có)         │
        └────┬─────────────┘
             │
             ▼
        ┌──────────────────┐
        │ Tính total       │
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

**Status:** ✅ HOÀN THÀNH
**Ngày:** 2025-01-20
**Sẵn sàng:** Test API
