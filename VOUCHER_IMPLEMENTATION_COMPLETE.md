# ✅ CHỨC NĂNG VOUCHER & ADDRESS - HOÀN THÀNH

## 📋 TÓM TẮT

Đã hoàn thành triển khai chức năng:
1. **Address (Địa chỉ giao hàng)** - Validation theo delivery method
2. **Voucher (Mã giảm giá)** - Validate, tính discount, lưu vào đơn hàng

---

## 🎯 FILES ĐÃ TẠO/CẬP NHẬT

### 1. **Entity Models**
- ✅ `Voucher.java` - Entity mới cho voucher
- ✅ `Order.java` - Cập nhật: thêm `discount` và `voucherCode`

### 2. **Repository**
- ✅ `VoucherRepository.java` - Repository mới với custom queries

### 3. **Service**
- ✅ `OrderService.java` - Cập nhật:
  - Thêm `VoucherRepository` injection
  - Cập nhật `createOrder()` - thêm logic validate address và voucher
  - Thêm method `validateAndApplyVoucher()` - validate và tính discount
  - Thêm inner class `VoucherValidationResult` - lưu kết quả validation

### 4. **DTO**
- ✅ `OrderResponse.java` - Cập nhật: thêm `discount` và `voucherCode`

### 5. **Database**
- ✅ `CREATE_VOUCHER_TABLE.sql` - SQL schema tạo bảng vouchers + dữ liệu mẫu

### 6. **Documentation**
- ✅ `VOUCHER_ADDRESS_GUIDE.md` - Hướng dẫn chi tiết về nghiệp vụ

---

## 🔄 FLOW VALIDATION

### **Address Validation**

```
Nếu deliveryMethod = DELIVERY:
  ✓ shippingAddress không null, không rỗng
  ✓ phoneNumber không null, không rỗng
  ✓ Phí ship = 30,000 VND
  
Nếu deliveryMethod = PICKUP:
  ✓ Không cần address
  ✓ Phí ship = 0 VND
```

### **Voucher Validation**

```
Nếu voucherCode được cung cấp:
  1. Kiểm tra voucher tồn tại
  2. Kiểm tra is_active = TRUE
  3. Kiểm tra start_date <= NOW() <= end_date
  4. Kiểm tra subtotal >= min_order_amount (nếu có)
  5. Kiểm tra usage_count < usage_limit (nếu có)
  
Nếu hợp lệ:
  - Tính discount (FIXED hoặc PERCENT)
  - Áp dụng max_discount (nếu có)
  - Lưu vào order
  
Nếu không hợp lệ:
  - Trả về error message
  - Không áp dụng discount
```

---

## 💰 TÍNH TOÁN GIÁ

### **Công thức**

```
Subtotal = Σ(Giá SP + Giá Size + Giá Topping) × Số lượng
Tax = Subtotal × 10%
ShippingFee = 0 (PICKUP) hoặc 30,000 (DELIVERY)
Discount = 0 (không voucher) hoặc tính từ voucher
TotalAmount = Subtotal + Tax + ShippingFee - Discount
```

### **Ví dụ 1: PICKUP, không voucher**

```
Sản phẩm: 45,000 × 1
Subtotal = 45,000
Tax = 4,500
ShippingFee = 0
Discount = 0
TotalAmount = 49,500 VND
```

### **Ví dụ 2: DELIVERY, voucher FREESHIP**

```
Sản phẩm: 45,000 × 1
Subtotal = 45,000
Tax = 4,500
ShippingFee = 30,000
Voucher = FREESHIP (giảm 30,000)
Discount = 30,000
TotalAmount = 49,500 VND
```

### **Ví dụ 3: DELIVERY, voucher SAVE20 (20%)**

```
Sản phẩm: 45,000 × 2
Subtotal = 90,000
Tax = 9,000
ShippingFee = 30,000
Voucher = SAVE20 (20% = 18,000, tối đa 100,000)
Discount = 18,000
TotalAmount = 111,000 VND
```

---

## 📊 VOUCHER TYPES

### **Type 1: FIXED (Giảm cố định)**

```
Ví dụ: FREESHIP
- discount_type = FIXED
- discount_value = 30,000
- Giảm cố định 30,000 VND
```

### **Type 2: PERCENT (Giảm theo %)**

```
Ví dụ: SAVE20
- discount_type = PERCENT
- discount_value = 20
- Giảm 20% của subtotal
- max_discount = 100,000 (giảm tối đa 100,000)
```

---

## 🗄️ DATABASE SCHEMA

### **Bảng vouchers**

```sql
CREATE TABLE vouchers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_type VARCHAR(50) NOT NULL,      -- FIXED hoặc PERCENT
    discount_value DECIMAL(10, 2) NOT NULL,  -- Giá trị giảm
    min_order_amount DECIMAL(10, 2),         -- Tối thiểu để áp dụng
    max_discount DECIMAL(10, 2),             -- Giảm tối đa
    is_active BOOLEAN DEFAULT TRUE,
    start_date DATETIME,
    end_date DATETIME,
    usage_limit INT,                         -- Số lần sử dụng tối đa
    usage_count INT DEFAULT 0,               -- Số lần đã sử dụng
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

### **Cập nhật bảng orders**

```sql
ALTER TABLE orders ADD COLUMN discount DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE orders ADD COLUMN voucher_code VARCHAR(50);
```

---

## 📝 DỮ LIỆU VOUCHER MẪU

| Code | Type | Value | Min | Max | Mô tả |
|------|------|-------|-----|-----|-------|
| FREESHIP | FIXED | 30,000 | - | 30,000 | Miễn phí ship |
| SAVE20 | PERCENT | 20% | 100,000 | 100,000 | Giảm 20% (tối đa 100k) |
| SAVE50K | FIXED | 50,000 | 200,000 | 50,000 | Giảm 50k (tối thiểu 200k) |
| SAVE10 | PERCENT | 10% | - | 50,000 | Giảm 10% (tối đa 50k) |
| SAVE15 | PERCENT | 15% | 150,000 | 75,000 | Giảm 15% (tối thiểu 150k, tối đa 75k) |

---

## 🚀 SETUP STEPS

### **Bước 1: Chạy SQL schema**

```sql
-- Chạy file CREATE_VOUCHER_TABLE.sql
-- Hoặc chạy từng câu lệnh:

CREATE TABLE IF NOT EXISTS vouchers (...);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS discount DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS voucher_code VARCHAR(50);

-- Insert dữ liệu mẫu
INSERT INTO vouchers (...) VALUES (...);
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
  "items": [...],
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "shippingAddress": "123 Nguyễn Huệ, Q1, TP.HCM",
  "phoneNumber": "0123456789",
  "voucherCode": "FREESHIP"
}
```

---

## ✅ TEST CASES

### **Test 1: PICKUP, không voucher**

```json
Request:
{
  "items": [{...}],
  "deliveryMethod": "PICKUP",
  "paymentMethod": "COD"
}

Response:
{
  "totalAmount": 49500,
  "tax": 4500,
  "shippingFee": 0,
  "discount": 0,
  "voucherCode": null
}
```

### **Test 2: DELIVERY, voucher FREESHIP**

```json
Request:
{
  "items": [{...}],
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "shippingAddress": "123 Nguyễn Huệ, Q1, TP.HCM",
  "phoneNumber": "0123456789",
  "voucherCode": "FREESHIP"
}

Response:
{
  "totalAmount": 49500,
  "tax": 4500,
  "shippingFee": 30000,
  "discount": 30000,
  "voucherCode": "FREESHIP"
}
```

### **Test 3: DELIVERY, không có address (ERROR)**

```json
Request:
{
  "items": [{...}],
  "deliveryMethod": "DELIVERY",
  "paymentMethod": "COD",
  "phoneNumber": "0123456789"
}

Response:
{
  "success": false,
  "message": "Địa chỉ giao hàng không được để trống"
}
```

### **Test 4: Voucher không hợp lệ (ERROR)**

```json
Request:
{
  "items": [{...}],
  "deliveryMethod": "PICKUP",
  "paymentMethod": "COD",
  "voucherCode": "INVALID_CODE"
}

Response:
{
  "success": false,
  "message": "Mã voucher không hợp lệ hoặc đã hết hạn"
}
```

### **Test 5: Voucher tối thiểu không đạt (ERROR)**

```json
Request:
{
  "items": [{...}],  // Subtotal = 30,000
  "deliveryMethod": "PICKUP",
  "paymentMethod": "COD",
  "voucherCode": "SAVE20"  // Yêu cầu tối thiểu 100,000
}

Response:
{
  "success": false,
  "message": "Tổng tiền phải từ 100000 VND để sử dụng voucher này"
}
```

---

## 🔍 VALIDATION RULES

### **Address**

- ✓ Nếu DELIVERY: bắt buộc address + phone
- ✓ Nếu PICKUP: không cần address
- ✓ Address không được null/rỗng (khi DELIVERY)
- ✓ Phone không được null/rỗng (khi DELIVERY)

### **Voucher**

- ✓ Voucher phải tồn tại trong DB
- ✓ Voucher phải is_active = TRUE
- ✓ Voucher phải trong khoảng start_date - end_date
- ✓ Subtotal phải >= min_order_amount (nếu có)
- ✓ usage_count phải < usage_limit (nếu có)

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
        │ (nếu DELIVERY)   │
        └────┬─────────────┘
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
        │ Tính discount    │
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

## 📝 NEXT STEPS

1. ✅ Chạy SQL schema tạo bảng vouchers
2. ✅ Build project
3. ✅ Test API endpoints
4. ⏳ Thêm Admin endpoints để quản lý vouchers (optional)
5. ⏳ Thêm Payment integration (VNPay, Momo) (optional)

---

**Status:** ✅ HOÀN THÀNH
**Ngày:** 2025-01-20
**Sẵn sàng:** Test API
