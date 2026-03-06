# 📦 Order API Documentation

## 🎯 Tổng quan

Hệ thống Order được chia thành 2 loại:
1. **User Order** - Đơn hàng từ khách online (có address, voucher, phí ship)
2. **Staff Order** - Đơn hàng từ staff/admin tại quán (có thuế 10%, không phí ship)

---

## 🔐 Authentication

Tất cả API yêu cầu **JWT Token** trong header:
```
Authorization: Bearer <your_jwt_token>
```

---

## 👤 USER ORDER APIs

### 1️⃣ Preview Giá Đơn Hàng (Chưa tạo đơn)

**Endpoint:** `POST /api/orders/preview`

**Purpose:** User xem giá trước khi đặt hàng (chưa tạo đơn)

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "sizeId": 2,
      "quantity": 2,
      "toppingIds": [1, 2],
      "iceOptionId": 1,
      "notes": "Ít đá"
    }
  ],
  "addressId": 5,
  "voucherCode": "SAVE50K",
  "deliveryMethod": "DELIVERY"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Preview giá thành công",
  "data": {
    "subtotal": 100000,
    "tax": 0,
    "shippingFee": 30000,
    "discount": 50000,
    "voucherCode": "SAVE50K",
    "totalAmount": 80000,
    "items": [
      {
        "productId": 1,
        "sizeId": 2,
        "quantity": 2,
        "toppingIds": "[1, 2]",
        "iceOptionId": 1,
        "notes": "Ít đá"
      }
    ],
    "message": "Giá có thể thay đổi khi đặt hàng. Vui lòng xác nhận trước khi thanh toán."
  }
}
```

**Tính toán giá:**
- **Subtotal** = (Giá SP + Giá Size + Giá Topping) × Số lượng
- **Tax** = 0% (User order không có thuế)
- **ShippingFee** = Tính theo khu vực (từ ShippingZone)
- **Discount** = Áp dụng voucher (nếu có)
- **Total** = Subtotal + ShippingFee - Discount

**Lưu ý:**
- ✅ Không tạo đơn, chỉ tính giá
- ✅ Voucher có thể không hợp lệ (không throw error, chỉ log warning)
- ✅ Giá có thể thay đổi khi đặt hàng (tính lại để bảo vệ)

---

### 2️⃣ Tạo Đơn Hàng (User)

**Endpoint:** `POST /api/orders/create`

**Purpose:** Khách đặt hàng online

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "sizeId": 2,
      "quantity": 2,
      "toppingIds": [1, 2],
      "iceOptionId": 1,
      "notes": "Ít đá"
    }
  ],
  "addressId": 5,
  "voucherCode": "SAVE50K",
  "paymentMethod": "CREDIT_CARD",
  "deliveryMethod": "DELIVERY",
  "notes": "Giao trước 5h chiều"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo đơn hàng thành công",
  "data": {
    "id": 123,
    "orderCode": "ORD1732169200000",
    "totalAmount": 80000,
    "tax": 0,
    "shippingFee": 30000,
    "discount": 50000,
    "voucherCode": "SAVE50K",
    "status": "PENDING",
    "paymentMethod": "CREDIT_CARD",
    "deliveryMethod": "DELIVERY",
    "addressId": 5,
    "notes": "Giao trước 5h chiều",
    "createdAt": "2025-11-21T10:53:20"
  }
}
```

**Validation:**
- ✅ Items không được rỗng
- ✅ AddressId bắt buộc
- ✅ Address phải thuộc user
- ✅ Voucher hợp lệ (nếu có)
- ✅ Tính lại giá (bảo vệ chống thay đổi giá)

**Status Flow:**
```
PENDING → (Staff duyệt) → CONFIRMED → SHIPPING → DELIVERED
```

---

### 3️⃣ Lấy Danh Sách Đơn Hàng (User)

**Endpoint:** `GET /api/orders`

**Purpose:** Khách xem danh sách đơn của mình

**Query Parameters:**
```
?page=0&size=10&sort=createdAt,desc
```

**Response:**
```json
{
  "success": true,
  "message": "Lấy danh sách đơn hàng thành công",
  "data": {
    "content": [
      {
        "id": 123,
        "orderCode": "ORD1732169200000",
        "totalAmount": 80000,
        "status": "PENDING",
        "createdAt": "2025-11-21T10:53:20"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

---

### 4️⃣ Lấy Chi Tiết Đơn Hàng (User)

**Endpoint:** `GET /api/orders/{id}`

**Purpose:** Khách xem chi tiết đơn

**Response:**
```json
{
  "success": true,
  "message": "Lấy chi tiết đơn hàng thành công",
  "data": {
    "id": 123,
    "orderCode": "ORD1732169200000",
    "totalAmount": 80000,
    "tax": 0,
    "shippingFee": 30000,
    "discount": 50000,
    "voucherCode": "SAVE50K",
    "status": "PENDING",
    "paymentMethod": "CREDIT_CARD",
    "deliveryMethod": "DELIVERY",
    "addressId": 5,
    "address": {
      "id": 5,
      "fullName": "Nguyễn Văn A",
      "phone": "0987654321",
      "district": "Quận 3",
      "city": "TP.HCM",
      "fullAddress": "456 Lê Lợi, Quận 3, TP.HCM"
    },
    "notes": "Giao trước 5h chiều",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Cà Phê Đen",
        "sizeId": 2,
        "sizeName": "Lớn",
        "quantity": 2,
        "price": 25000,
        "sizePrice": 5000,
        "toppingPrice": 10000,
        "toppingNames": "Trân Châu, Thạch",
        "toppingIds": "[1, 2]",
        "iceOptionId": 1,
        "iceOptionName": "Ít đá",
        "notes": "Ít đá",
        "subtotal": 80000
      }
    ],
    "statusLogs": [
      {
        "id": 1,
        "status": "PENDING",
        "reason": "Đơn hàng vừa được tạo",
        "createdAt": "2025-11-21T10:53:20"
      }
    ],
    "createdAt": "2025-11-21T10:53:20"
  }
}
```

---

### 5️⃣ Hủy Đơn Hàng (User)

**Endpoint:** `PUT /api/orders/{id}/cancel`

**Purpose:** Khách hủy đơn

**Request Body:**
```json
{
  "reason": "Tôi muốn hủy đơn"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Hủy đơn hàng thành công",
  "data": {
    "id": 123,
    "status": "CANCELLED"
  }
}
```

**Điều kiện:**
- ✅ Chỉ hủy được khi status = PENDING
- ✅ Sau khi hủy → CANCELLED

---

### 6️⃣ Lấy Lịch Sử Trạng Thái Đơn (User)

**Endpoint:** `GET /api/orders/{id}/status-log`

**Purpose:** Xem lịch sử thay đổi trạng thái

**Response:**
```json
{
  "success": true,
  "message": "Lấy lịch sử trạng thái thành công",
  "data": [
    {
      "id": 1,
      "orderId": 123,
      "status": "PENDING",
      "reason": "Đơn hàng vừa được tạo",
      "createdAt": "2025-11-21T10:53:20"
    },
    {
      "id": 2,
      "orderId": 123,
      "status": "CONFIRMED",
      "reason": "Đơn hàng được xác nhận",
      "createdAt": "2025-11-21T11:00:00"
    }
  ]
}
```

---

## 👨‍💼 STAFF/ADMIN ORDER APIs

### 1️⃣ Tạo Đơn Hàng Tại Quán (Staff)

**Endpoint:** `POST /api/admin/orders`

**Purpose:** Staff tạo đơn tại quán

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "sizeId": 2,
      "quantity": 2,
      "toppingIds": [1, 2],
      "iceOptionId": 1,
      "notes": "Ít đá"
    }
  ],
  "paymentMethod": "CASH",
  "notes": "Khách VIP",
  "createdByStaffId": 10
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo đơn hàng thành công",
  "data": {
    "id": 124,
    "orderCode": "ORD1732169300000",
    "totalAmount": 110000,
    "tax": 10000,
    "shippingFee": 0,
    "discount": 0,
    "status": "PENDING",
    "paymentMethod": "CASH",
    "deliveryMethod": "PICKUP",
    "notes": "Khách VIP",
    "createdAt": "2025-11-21T10:54:00"
  }
}
```

**Tính toán giá (Staff Order):**
- **Subtotal** = (Giá SP + Giá Size + Giá Topping) × Số lượng
- **Tax** = Subtotal × 10% (Có thuế VAT)
- **ShippingFee** = 0 (Không có phí ship)
- **Discount** = 0 (Không voucher)
- **Total** = Subtotal + Tax

**Lưu ý:**
- ✅ Mặc định DeliveryMethod = PICKUP (tại quán)
- ✅ Không có voucher
- ✅ Có thuế 10%
- ✅ createdByStaffId dùng cho audit trail

---

### 2️⃣ Lấy Danh Sách Tất Cả Đơn Hàng (Staff)

**Endpoint:** `GET /api/admin/orders`

**Purpose:** Staff xem tất cả đơn (không chỉ của mình)

**Query Parameters:**
```
?page=0&size=10&sort=createdAt,desc
```

**Response:**
```json
{
  "success": true,
  "message": "Lấy danh sách đơn hàng thành công",
  "data": {
    "content": [
      {
        "id": 123,
        "orderCode": "ORD1732169200000",
        "totalAmount": 80000,
        "status": "PENDING",
        "createdAt": "2025-11-21T10:53:20"
      },
      {
        "id": 124,
        "orderCode": "ORD1732169300000",
        "totalAmount": 110000,
        "status": "CONFIRMED",
        "createdAt": "2025-11-21T10:54:00"
      }
    ],
    "totalElements": 2,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

---

### 3️⃣ Lấy Chi Tiết Đơn Hàng (Staff)

**Endpoint:** `GET /api/admin/orders/{id}`

**Purpose:** Staff xem chi tiết đơn

**Response:** (Giống User, nhưng không kiểm tra quyền sở hữu)

---

### 4️⃣ Xác Nhận Đơn Hàng (PENDING → CONFIRMED)

**Endpoint:** `PUT /api/admin/orders/{id}/confirm`

**Purpose:** Staff xác nhận đơn

**Query Parameters:**
```
?reason=Đơn hàng hợp lệ
```

**Response:**
```json
{
  "success": true,
  "message": "Xác nhận đơn hàng thành công",
  "data": {
    "id": 123,
    "status": "CONFIRMED"
  }
}
```

**Status Flow:**
```
PENDING → CONFIRMED
```

---

### 5️⃣ Hoàn Thành Đơn Hàng (CONFIRMED → COMPLETED) - PICKUP

**Endpoint:** `PUT /api/admin/orders/{id}/complete`

**Purpose:** Staff hoàn thành đơn PICKUP (khách đã nhận)

**Query Parameters:**
```
?reason=Khách đã nhận hàng
```

**Response:**
```json
{
  "success": true,
  "message": "Hoàn thành đơn hàng thành công",
  "data": {
    "id": 124,
    "status": "COMPLETED"
  }
}
```

**Điều kiện:**
- ✅ Status phải = CONFIRMED
- ✅ DeliveryMethod phải = PICKUP

---

### 6️⃣ Bắt Đầu Giao Hàng (CONFIRMED → SHIPPING) - DELIVERY

**Endpoint:** `PUT /api/admin/orders/{id}/ship`

**Purpose:** Staff bắt đầu giao đơn DELIVERY

**Query Parameters:**
```
?reason=Đơn hàng đang được giao
```

**Response:**
```json
{
  "success": true,
  "message": "Bắt đầu giao hàng thành công",
  "data": {
    "id": 123,
    "status": "SHIPPING"
  }
}
```

**Status Flow:**
```
CONFIRMED → SHIPPING
```

---

### 7️⃣ Giao Hàng Thành Công (SHIPPING → DELIVERED)

**Endpoint:** `PUT /api/admin/orders/{id}/deliver`

**Purpose:** Shipper giao thành công

**Query Parameters:**
```
?reason=Đơn hàng đã giao thành công
```

**Response:**
```json
{
  "success": true,
  "message": "Giao hàng thành công",
  "data": {
    "id": 123,
    "status": "DELIVERED"
  }
}
```

**Status Flow:**
```
SHIPPING → DELIVERED
```

---

### 8️⃣ Từ Chối Đơn Hàng (PENDING → CANCELLED)

**Endpoint:** `PUT /api/admin/orders/{id}/reject`

**Purpose:** Staff từ chối đơn

**Query Parameters:**
```
?reason=Sản phẩm hết hàng
```

**Response:**
```json
{
  "success": true,
  "message": "Từ chối đơn hàng thành công",
  "data": {
    "id": 123,
    "status": "CANCELLED"
  }
}
```

**Điều kiện:**
- ✅ Status phải = PENDING
- ✅ Sau khi từ chối → CANCELLED

---

## 📊 Status Flow Diagram

### User Order (Online)
```
PENDING (Chờ staff duyệt)
    ↓
CONFIRMED (Staff xác nhận)
    ↓
SHIPPING (Đang giao)
    ↓
DELIVERED (Giao thành công)
```

### Staff Order (Tại quán)
```
PENDING (Chờ staff xác nhận)
    ↓
CONFIRMED (Staff xác nhận)
    ↓
COMPLETED (Khách đã nhận)
```

---

## 🔍 Comparison: User vs Staff Order

| Tiêu chí | User Order | Staff Order |
|---------|-----------|------------|
| **Address** | Bắt buộc | Không cần |
| **Voucher** | Có thể có | Không có |
| **Tax** | 0% | 10% |
| **Shipping Fee** | Có (theo khu vực) | Không (0) |
| **Delivery Method** | DELIVERY hoặc PICKUP | PICKUP (mặc định) |
| **Payment** | Trực tuyến | Tiền mặt |
| **Status Flow** | PENDING → CONFIRMED → SHIPPING → DELIVERED | PENDING → CONFIRMED → COMPLETED |

---

## ⚠️ Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Danh sách sản phẩm không được để trống",
  "data": null
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Bạn không có quyền truy cập",
  "data": null
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Đơn hàng không tồn tại",
  "data": null
}
```

---

## 🧪 Testing với Postman

### 1. User Preview Giá
```
POST http://localhost:3000/api/orders/preview
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "items": [{"productId": 1, "sizeId": 2, "quantity": 1, "toppingIds": [1]}],
  "addressId": 5,
  "voucherCode": "SAVE50K",
  "deliveryMethod": "DELIVERY"
}
```

### 2. Staff Tạo Đơn
```
POST http://localhost:3000/api/admin/orders
Authorization: Bearer <staff_token>
Content-Type: application/json

{
  "items": [{"productId": 1, "sizeId": 2, "quantity": 1}],
  "paymentMethod": "CASH",
  "createdByStaffId": 10
}
```

### 3. Staff Xác Nhận Đơn
```
PUT http://localhost:3000/api/admin/orders/123/confirm?reason=Đơn hợp lệ
Authorization: Bearer <staff_token>
```

---

## 📝 Notes

- ✅ Tất cả API yêu cầu JWT token
- ✅ User chỉ xem được đơn của mình
- ✅ Staff/Admin xem được tất cả đơn
- ✅ Giá được tính lại khi tạo đơn (bảo vệ)
- ✅ Lịch sử trạng thái được ghi log tự động
- ✅ Audit trail: createdByStaffId cho staff order

---

**Last Updated:** 2025-11-21
