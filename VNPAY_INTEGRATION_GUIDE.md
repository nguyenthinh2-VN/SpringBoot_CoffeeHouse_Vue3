# VNPAY Integration Guide

## Overview
VNPAY payment integration implemented using **Strategy Pattern** and **Single Responsibility Principle**.

## Architecture

### Components Created

#### 1. Configuration Layer
- **VNPayConfig** (`Config/VNPayConfig.java`)
  - Loads VNPAY credentials from environment variables
  - Validates configuration
  - Provides access to VNPAY settings

#### 2. Service Layer

**VNPayValidator** (`Service/Payment/VNPayValidator.java`)
- Validates HMAC SHA512 signatures from VNPAY callbacks
- Single Responsibility: Signature verification

**VNPayService** (`Service/Payment/VNPayService.java`)
- Builds VNPAY payment URLs
- Parses VNPAY callback responses
- Single Responsibility: VNPAY API communication

**PaymentService** (`Service/Payment/PaymentService.java`)
- Orchestrates payment processing
- Routes to appropriate payment processor
- Single Responsibility: Payment coordination

#### 3. Strategy Pattern - Payment Processors

**PaymentProcessor** (Interface)
- Defines contract for payment methods
- Methods: `processPayment()`, `handleCallback()`, `getPaymentMethod()`

**VNPayPaymentProcessor** (`Service/Payment/VNPayPaymentProcessor.java`)
- Implements VNPAY payment processing
- Handles VNPAY callbacks
- Updates order payment status

**CODPaymentProcessor** (`Service/Payment/CODPaymentProcessor.java`)
- Implements COD (Cash On Delivery) payment
- Handles COD confirmation

**PaymentProcessorFactory** (`Service/Payment/PaymentProcessorFactory.java`)
- Factory pattern to get appropriate processor
- Registers all payment processors
- Single Responsibility: Processor instantiation

#### 4. DTOs

**VNPayPaymentRequest** (`DTO/Payment/VNPayPaymentRequest.java`)
- Order ID, code, amount, IP address
- Converts amount to VNPAY format (multiply by 100)

**VNPayPaymentResponse** (`DTO/Payment/VNPayPaymentResponse.java`)
- Payment result from VNPAY
- Response code, transaction ID, amount, bank info
- `isSuccess()` method to check payment status

**PaymentResult** (`Service/Payment/PaymentResult.java`)
- Generic payment processing result
- Contains redirect URL for VNPAY
- Transaction ID and additional data

**PaymentCallbackResult** (`Service/Payment/PaymentCallbackResult.java`)
- Callback handling result
- Payment status, transaction ID, amount

#### 5. Controller Endpoints

**OrderController** additions:
- `POST /orders/{orderId}/vnpay-payment` - Create VNPAY payment link
- `GET /orders/vnpay-callback` - Handle VNPAY callback

## Flow Diagram

```
User Creates Order (VNPAY)
        ↓
POST /orders/create
        ↓
Order saved with paymentStatus=PENDING
        ↓
User clicks "Pay with VNPAY"
        ↓
POST /orders/{orderId}/vnpay-payment
        ↓
PaymentService.processPayment(order)
        ↓
PaymentProcessorFactory.getProcessor(VNPAY)
        ↓
VNPayPaymentProcessor.processPayment()
        ↓
VNPayService.buildPaymentUrl()
        ↓
Return VNPAY payment URL
        ↓
User redirected to VNPAY gateway
        ↓
User completes payment
        ↓
VNPAY redirects to callback URL
        ↓
GET /orders/vnpay-callback?vnp_*=...
        ↓
PaymentService.handleCallback(VNPAY, params)
        ↓
VNPayPaymentProcessor.handleCallback()
        ↓
VNPayValidator.validateSignature()
        ↓
Update Order: paymentStatus=PAID
        ↓
Return success response
```

## Configuration

### Environment Variables Required

```bash
# VNPAY Credentials (from VNPAY dashboard)
vnpay.tmn.code=YOUR_TMN_CODE
vnpay.hash.secret=YOUR_HASH_SECRET

# Optional (defaults provided)
vnpay.api.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return.url=http://localhost:3000/orders/vnpay-callback
```

### application.properties

```properties
vnpay.tmnCode=${vnpay.tmn.code:}
vnpay.hashSecret=${vnpay.hash.secret:}
vnpay.apiUrl=${vnpay.api.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}
vnpay.returnUrl=${vnpay.return.url:http://localhost:3000/orders/vnpay-callback}
vnpay.version=2.1.0
vnpay.command=pay
vnpay.orderType=other
```

## API Usage

### 1. Create Order with VNPAY Payment

```bash
POST /orders/create
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "items": [
    {
      "productId": 1,
      "sizeId": 1,
      "quantity": 2,
      "toppingIds": [1, 2],
      "iceOptionId": 1,
      "notes": "Ít đá"
    }
  ],
  "deliveryMethod": "PICKUP",
  "paymentMethod": "VNPAY",
  "notes": "Giao lúc 10h"
}

Response:
{
  "success": true,
  "message": "Tạo đơn hàng thành công",
  "data": {
    "id": 30,
    "orderCode": "ORD1234567890",
    "totalAmount": 150000,
    "paymentStatus": "PENDING",
    "status": "PENDING"
  }
}
```

### 2. Create VNPAY Payment Link

```bash
POST /orders/30/vnpay-payment
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "success": true,
  "message": "Tạo link thanh toán thành công",
  "data": {
    "success": true,
    "message": "Payment link created successfully",
    "redirectUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Version=2.1.0&vnp_Command=pay&...",
    "transactionId": "ORD1234567890"
  }
}
```

### 3. VNPAY Callback (Automatic)

VNPAY redirects user to:
```
GET /orders/vnpay-callback?vnp_Amount=15000000&vnp_BankCode=NCB&vnp_BankTranNo=...&vnp_CardType=ATM&vnp_OrderInfo=Thanh+toan+don+hang%3A+ORD1234567890&vnp_PayDate=20251127091234&vnp_ResponseCode=00&vnp_SecureHash=...&vnp_TmnCode=YOUR_TMN&vnp_TransactionNo=14123456&vnp_TxnRef=ORD1234567890
```

Response:
```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "data": "OK"
}
```

## SRP Implementation

Each class has **single responsibility**:

| Class | Responsibility |
|-------|-----------------|
| VNPayConfig | Load & validate configuration |
| VNPayValidator | Verify HMAC signatures |
| VNPayService | VNPAY API communication |
| VNPayPaymentProcessor | VNPAY payment processing |
| CODPaymentProcessor | COD payment processing |
| PaymentProcessorFactory | Create processor instances |
| PaymentService | Coordinate payment flow |
| OrderController | Handle HTTP requests |

## Adding New Payment Methods

To add a new payment method (e.g., MOMO):

1. **Create Processor**
```java
@Component
public class MOMOPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentResult processPayment(Order order) { ... }
    
    @Override
    public PaymentCallbackResult handleCallback(Map<String, String> params) { ... }
    
    @Override
    public String getPaymentMethod() { return "MOMO"; }
}
```

2. **Factory automatically registers it**
```java
// Factory auto-discovers and registers MOMO processor
```

3. **Use it**
```java
paymentService.processPayment(order); // Works for any payment method
```

## Testing

### Test VNPAY Payment Flow

```bash
# 1. Create order
curl -X POST http://localhost:3000/orders/create \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{...}'

# 2. Get payment link
curl -X POST http://localhost:3000/orders/30/vnpay-payment \
  -H "Authorization: Bearer <TOKEN>"

# 3. Copy redirectUrl and open in browser
# 4. Complete payment in VNPAY sandbox
# 5. Callback automatically updates order
```

### Verify Payment Status

```bash
curl -X GET http://localhost:3000/orders/30 \
  -H "Authorization: Bearer <TOKEN>"

# Check paymentStatus field
```

## Security Considerations

1. **HMAC Signature Verification**
   - All VNPAY callbacks are verified using HMAC SHA512
   - Invalid signatures are rejected

2. **Order Ownership Check**
   - Only order owner can create payment link
   - Verified via JWT token

3. **Amount Validation**
   - Amount is converted to cents (multiply by 100)
   - Verified in callback

4. **Credentials Management**
   - VNPAY credentials stored in environment variables
   - Never hardcoded in source code

## Troubleshooting

### Issue: "VNPAY is not configured"
**Solution**: Set environment variables:
```bash
export vnpay.tmn.code=YOUR_CODE
export vnpay.hash.secret=YOUR_SECRET
```

### Issue: "Invalid signature"
**Solution**: 
- Verify hash secret is correct
- Check parameter order (must be alphabetical)
- Ensure amount is in cents

### Issue: Callback not received
**Solution**:
- Verify returnUrl is correct and accessible
- Check VNPAY merchant account settings
- Ensure firewall allows VNPAY IPs

## Next Steps

1. **Get VNPAY Merchant Account**
   - Register at https://vnpayment.vn
   - Get TMN Code and Hash Secret

2. **Set Environment Variables**
   ```bash
   export vnpay.tmn.code=YOUR_CODE
   export vnpay.hash.secret=YOUR_SECRET
   export vnpay.return.url=YOUR_RETURN_URL
   ```

3. **Test Integration**
   - Use VNPAY sandbox for testing
   - Verify payment flow end-to-end

4. **Deploy to Production**
   - Switch to production VNPAY URL
   - Update environment variables
   - Monitor payment transactions

## References

- VNPAY Documentation: https://vnpayment.vn
- VNPAY Integration Guide: https://sandbox.vnpayment.vn/apis/docs
- Strategy Pattern: https://refactoring.guru/design-patterns/strategy
- Single Responsibility Principle: https://en.wikipedia.org/wiki/Single-responsibility_principle
