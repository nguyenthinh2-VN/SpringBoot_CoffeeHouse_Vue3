package SpringBoot.demo.Service.Payment;

/**
 * Payment Callback Result DTO
 * Contains result of payment callback handling
 */
public class PaymentCallbackResult {
    
    private boolean success;
    private String message;
    private String orderCode;
    private String paymentStatus;      // PAID, FAILED, PENDING
    private String transactionId;
    private Long amount;
    private Object additionalData;
    
    // Constructors
    public PaymentCallbackResult() {}
    
    public PaymentCallbackResult(boolean success, String message, String orderCode) {
        this.success = success;
        this.message = message;
        this.orderCode = orderCode;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getOrderCode() {
        return orderCode;
    }
    
    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public Object getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(Object additionalData) {
        this.additionalData = additionalData;
    }
}
