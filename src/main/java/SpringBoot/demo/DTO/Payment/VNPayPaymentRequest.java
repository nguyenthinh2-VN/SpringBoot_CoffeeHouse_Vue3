package SpringBoot.demo.DTO.Payment;

/**
 * VNPAY Payment Request DTO
 * Contains information needed to create a VNPAY payment link
 */
public class VNPayPaymentRequest {
    
    private Long orderId;
    private String orderCode;
    private Long amount;           // Amount in VND
    private String ipAddress;
    private String description;
    
    // Constructors
    public VNPayPaymentRequest() {}
    
    public VNPayPaymentRequest(Long orderId, String orderCode, Long amount, String ipAddress) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.amount = amount;
        this.ipAddress = ipAddress;
    }
    
    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderCode() {
        return orderCode;
    }
    
    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
    
    public Long getAmount() {
        return amount * 100; // VNPAY requires amount in cents (multiply by 100)
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
