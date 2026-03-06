# 📋 Order Refactoring Plan - Staff/Admin vs User Orders

## 🎯 Vấn đề hiện tại

### Problem 1: Hai loại Order khác nhau
- **User Order (Online)**: Cần address, voucher, phí ship, không thuế
- **Staff/Admin Order (Tại quán)**: Không cần address, voucher, có thuế 10%, không phí ship

### Problem 2: Thiếu API preview giá
- FE cần API để xem giá trước khi đặt hàng (chưa tạo đơn)
- Sau đó tính lại khi tạo đơn (bảo vệ chống thay đổi giá)

---

## 🏗️ Kiến trúc Refactoring

### 1. Design Pattern: **Strategy Pattern** + **Abstract Class**

```
┌─────────────────────────────────────────┐
│     AbstractOrderProcessor (Abstract)    │
│  - calculatePrice() [abstract]           │
│  - validateOrder()                       │
│  - createOrder()                         │
│  - createStatusLog()                     │
└─────────────────────────────────────────┘
           ▲                    ▲
           │                    │
    ┌──────┴─────┐      ┌──────┴──────┐
    │             │      │              │
┌───┴────────┐  ┌┴──────┴────┐
│ UserOrder  │  │ StaffOrder │
│ Processor  │  │ Processor  │
│            │  │            │
│ - Tax: 0%  │  │ - Tax: 10% │
│ - Ship: ✅ │  │ - Ship: ❌ │
│ - Voucher: │  │ - Voucher: │
│   ✅       │  │   ❌       │
└────────────┘  └────────────┘
```

---

## 📁 File Structure

```
src/main/java/SpringBoot/demo/
├── Service/
│   ├── Order/
│   │   ├── AbstractOrderProcessor.java      [NEW - Abstract class]
│   │   ├── UserOrderProcessor.java          [NEW - User orders]
│   │   ├── StaffOrderProcessor.java         [NEW - Staff/Admin orders]
│   │   ├── OrderPricingService.java         [REFACTOR - Extract pricing logic]
│   │   ├── OrderItemService.java            [EXISTING]
│   │   └── OrderService.java                [REFACTOR - Orchestrator]
│   └── Admin/
│       └── AdminOrderService.java           [NEW - Staff/Admin operations]
│
├── Controller/
│   ├── OrderController.java                 [REFACTOR - User orders]
│   ├── AdminOrderController.java            [NEW - Staff/Admin orders]
│   └── OrderPreviewController.java          [NEW - Price preview]
│
├── DTO/
│   ├── Request/
│   │   ├── CreateUserOrderRequest.java      [NEW]
│   │   ├── CreateStaffOrderRequest.java     [NEW]
│   │   └── OrderPreviewRequest.java         [NEW]
│   └── Response/
│       ├── OrderPreviewResponse.java        [NEW]
│       └── OrderDetailResponse.java         [EXISTING]
│
└── Enum/
    └── OrderType.java                       [NEW - USER, STAFF]
```

---

## 🔄 API Endpoints

### User Orders (Khách online)
```
POST   /api/orders/preview              - Preview giá (chưa tạo đơn)
POST   /api/orders/create               - Tạo đơn hàng
GET    /api/orders                      - Danh sách đơn của user
GET    /api/orders/{id}                 - Chi tiết đơn
PUT    /api/orders/{id}/cancel          - Hủy đơn
GET    /api/orders/{id}/status-log      - Lịch sử trạng thái
```

### Staff/Admin Orders (Tại quán)
```
POST   /api/admin/orders                - Tạo đơn tại quán
GET    /api/admin/orders                - Danh sách tất cả đơn
GET    /api/admin/orders/{id}           - Chi tiết đơn
PUT    /api/admin/orders/{id}/confirm   - Xác nhận đơn
PUT    /api/admin/orders/{id}/complete  - Hoàn thành (PICKUP)
PUT    /api/admin/orders/{id}/ship      - Bắt đầu giao (DELIVERY)
PUT    /api/admin/orders/{id}/deliver   - Giao thành công
PUT    /api/admin/orders/{id}/reject    - Từ chối đơn
```

---

## 📊 Tính toán giá

### User Order (Online)
```
Subtotal = (Giá SP + Giá Size + Giá Topping) × Số lượng
Tax = 0% (không có)
ShippingFee = Tính theo khu vực (ShippingZone)
Discount = Áp dụng voucher (nếu có)
Total = Subtotal + ShippingFee - Discount
```

### Staff Order (Tại quán)
```
Subtotal = (Giá SP + Giá Size + Giá Topping) × Số lượng
Tax = Subtotal × 10%
ShippingFee = 0 (không có)
Discount = 0 (không voucher)
Total = Subtotal + Tax
```

---

## 🎨 Abstract Class Design

### AbstractOrderProcessor.java
```java
public abstract class AbstractOrderProcessor {
    
    protected OrderRepository orderRepository;
    protected OrderItemRepository orderItemRepository;
    protected OrderStatusLogRepository orderStatusLogRepository;
    protected OrderItemService orderItemService;
    
    // Abstract methods - Override in subclasses
    protected abstract BigDecimal calculateTax(BigDecimal subtotal);
    protected abstract BigDecimal calculateShippingFee(Order order, Address address);
    protected abstract BigDecimal calculateDiscount(String voucherCode, BigDecimal subtotal);
    protected abstract void validateOrderRequest(Object request);
    
    // Common methods
    public final ApiResponse<OrderResponse> processOrder(Object request, Long userId) {
        try {
            validateOrderRequest(request);
            
            // Calculate prices using abstract methods
            BigDecimal subtotal = calculateSubtotal(request);
            BigDecimal tax = calculateTax(subtotal);
            BigDecimal shippingFee = calculateShippingFee(null, null);
            BigDecimal discount = calculateDiscount(null, subtotal);
            BigDecimal totalAmount = subtotal.add(tax).add(shippingFee).subtract(discount);
            
            // Create order
            Order order = createOrder(request, userId, subtotal, tax, shippingFee, discount, totalAmount);
            Order savedOrder = orderRepository.save(order);
            
            // Save items
            saveOrderItems(request, savedOrder);
            
            // Create status log
            createStatusLog(savedOrder);
            
            return ApiResponse.success("Tạo đơn hàng thành công", mapToResponse(savedOrder));
        } catch (Exception e) {
            return ApiResponse.error("Lỗi: " + e.getMessage());
        }
    }
    
    protected abstract BigDecimal calculateSubtotal(Object request);
    protected abstract Order createOrder(Object request, Long userId, 
                                        BigDecimal subtotal, BigDecimal tax, 
                                        BigDecimal shippingFee, BigDecimal discount, 
                                        BigDecimal totalAmount);
    protected abstract void saveOrderItems(Object request, Order order);
    protected abstract OrderResponse mapToResponse(Order order);
    
    protected void createStatusLog(Order order) {
        OrderStatusLog log = new OrderStatusLog(order, order.getStatus(), "Đơn hàng vừa được tạo");
        orderStatusLogRepository.save(log);
    }
}
```

### UserOrderProcessor.java
```java
@Service
public class UserOrderProcessor extends AbstractOrderProcessor {
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private OrderPricingService orderPricingService;
    
    @Override
    protected BigDecimal calculateTax(BigDecimal subtotal) {
        return BigDecimal.ZERO; // User order không có thuế
    }
    
    @Override
    protected BigDecimal calculateShippingFee(Order order, Address address) {
        // Tính phí ship từ ShippingZone
        return orderPricingService.getShippingFee(address.getDistrict(), address.getCity())
                                  .getShippingFee();
    }
    
    @Override
    protected BigDecimal calculateDiscount(String voucherCode, BigDecimal subtotal) {
        if (voucherCode == null || voucherCode.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return orderPricingService.validateAndApplyVoucher(voucherCode, subtotal)
                                  .getDiscount();
    }
    
    @Override
    protected void validateOrderRequest(Object request) {
        CreateUserOrderRequest req = (CreateUserOrderRequest) request;
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm không được để trống");
        }
        if (req.getAddressId() == null) {
            throw new RuntimeException("Địa chỉ giao hàng không được để trống");
        }
    }
    
    // ... implement other abstract methods
}
```

### StaffOrderProcessor.java
```java
@Service
public class StaffOrderProcessor extends AbstractOrderProcessor {
    
    @Override
    protected BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.10"))
                      .setScale(0, RoundingMode.HALF_UP); // 10% thuế
    }
    
    @Override
    protected BigDecimal calculateShippingFee(Order order, Address address) {
        return BigDecimal.ZERO; // Staff order không có phí ship
    }
    
    @Override
    protected BigDecimal calculateDiscount(String voucherCode, BigDecimal subtotal) {
        return BigDecimal.ZERO; // Staff order không có voucher
    }
    
    @Override
    protected void validateOrderRequest(Object request) {
        CreateStaffOrderRequest req = (CreateStaffOrderRequest) request;
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm không được để trống");
        }
    }
    
    // ... implement other abstract methods
}
```

---

## 📝 DTO Definitions

### CreateUserOrderRequest.java
```java
@Data
public class CreateUserOrderRequest {
    private List<OrderItemRequest> items;
    private Long addressId;
    private String voucherCode;
    private PaymentMethod paymentMethod;
    private DeliveryMethod deliveryMethod;
    private String notes;
}
```

### CreateStaffOrderRequest.java
```java
@Data
public class CreateStaffOrderRequest {
    private List<OrderItemRequest> items;
    private PaymentMethod paymentMethod;
    private String notes;
    private Long createdByStaffId; // Staff ID tạo đơn
}
```

### OrderPreviewRequest.java
```java
@Data
public class OrderPreviewRequest {
    private List<OrderItemRequest> items;
    private Long addressId;              // Nullable (PICKUP không cần)
    private String voucherCode;
    private DeliveryMethod deliveryMethod;
}
```

### OrderPreviewResponse.java
```java
@Data
public class OrderPreviewResponse {
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingFee;
    private BigDecimal discount;
    private String voucherCode;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private String message; // "Giá có thể thay đổi khi đặt hàng"
}
```

---

## 🔄 Flow Diagram

### User Order Flow
```
1. FE: Chọn sản phẩm, nhập address, voucher
   ↓
2. API: POST /api/orders/preview
   ↓
3. BE: UserOrderProcessor.calculatePrice()
   - Tính subtotal
   - Tax = 0%
   - ShippingFee từ ShippingZone
   - Discount từ Voucher
   - Total = Subtotal + ShippingFee - Discount
   ↓
4. FE: Hiển thị giá preview cho user
   ↓
5. User bấm "Đặt hàng"
   ↓
6. API: POST /api/orders/create
   ↓
7. BE: Tính lại giá (bảo vệ)
   ↓
8. Tạo Order, OrderItem, OrderStatusLog
   ↓
9. Return OrderResponse
```

### Staff Order Flow
```
1. Staff: Chọn sản phẩm, tạo đơn
   ↓
2. API: POST /api/admin/orders
   ↓
3. BE: StaffOrderProcessor.calculatePrice()
   - Tính subtotal
   - Tax = 10%
   - ShippingFee = 0
   - Discount = 0
   - Total = Subtotal + Tax
   ↓
4. Tạo Order, OrderItem, OrderStatusLog
   ↓
5. Return OrderResponse
```

---

## 📋 Implementation Steps

### Phase 1: Setup Base Classes
- [ ] Create `AbstractOrderProcessor.java`
- [ ] Create `OrderType.java` enum
- [ ] Create DTOs (CreateUserOrderRequest, CreateStaffOrderRequest, OrderPreviewRequest, OrderPreviewResponse)

### Phase 2: Implement Processors
- [ ] Create `UserOrderProcessor.java`
- [ ] Create `StaffOrderProcessor.java`
- [ ] Refactor `OrderPricingService.java` (extract pricing logic)

### Phase 3: Create Controllers & Services
- [ ] Create `OrderPreviewController.java` (preview API)
- [ ] Refactor `OrderController.java` (use UserOrderProcessor)
- [ ] Create `AdminOrderController.java` (staff orders)
- [ ] Create `AdminOrderService.java` (staff operations)

### Phase 4: Update Existing Services
- [ ] Refactor `OrderService.java` (use processors)
- [ ] Update `OrderItemService.java` (if needed)

### Phase 5: Testing
- [ ] Unit tests for processors
- [ ] Integration tests for APIs
- [ ] Manual testing with Postman

---

## 🔐 Security & Validation

### User Order
- ✅ JWT token required
- ✅ Address must belong to user
- ✅ Voucher validation
- ✅ Price recalculation on create (prevent fraud)

### Staff Order
- ✅ JWT token required
- ✅ Staff/Admin role required
- ✅ Audit log (who created order)

---

## 📌 Key Points

1. **Abstract Class Pattern**: Giảm code duplication, dễ maintain
2. **Strategy Pattern**: Dễ thêm loại order mới (e.g., Corporate Order)
3. **Price Preview**: Bảo vệ chống thay đổi giá, UX tốt hơn
4. **Separation of Concerns**: User logic ≠ Staff logic
5. **Audit Trail**: Ghi log ai tạo đơn (staff)

---

## 🚀 Next Steps

1. Tạo file README này ✅
2. Tạo Abstract class + Processors
3. Tạo DTOs
4. Tạo Controllers
5. Test & Deploy

---

**Status**: 📝 Planning Phase
**Last Updated**: 2025-11-21
