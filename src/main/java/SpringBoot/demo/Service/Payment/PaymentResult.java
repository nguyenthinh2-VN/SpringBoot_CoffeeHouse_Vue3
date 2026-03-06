package SpringBoot.demo.Service.Payment;

/**
 * Payment Result DTO
 * Contains result of payment processing
 */
public class PaymentResult {
    
    private boolean success;
    private String message;
    private String redirectUrl;    // For VNPAY
    private String transactionId;
    private Object data;           // Additional data
    
    // Constructors
    public PaymentResult() {}
    
    public PaymentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public PaymentResult(boolean success, String message, String redirectUrl) {
        this.success = success;
        this.message = message;
        this.redirectUrl = redirectUrl;
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
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
}
