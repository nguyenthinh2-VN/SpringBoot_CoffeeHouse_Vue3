package SpringBoot.demo.DTO.Payment;

/**
 * VNPAY Payment Response DTO
 * Contains payment result from VNPAY callback
 */
public class VNPayPaymentResponse {
    
    private boolean valid;              // Signature valid
    private String orderCode;
    private String transactionId;
    private String responseCode;        // "00" = success
    private String message;
    private Long amount;                // Amount in VND
    private String payDate;
    private String bankCode;
    private String bankTranNo;
    
    // Constructors
    public VNPayPaymentResponse() {}
    
    public VNPayPaymentResponse(String orderCode, String responseCode, String message) {
        this.orderCode = orderCode;
        this.responseCode = responseCode;
        this.message = message;
    }
    
    // Getters and Setters
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public String getOrderCode() {
        return orderCode;
    }
    
    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getPayDate() {
        return payDate;
    }
    
    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getBankTranNo() {
        return bankTranNo;
    }
    
    public void setBankTranNo(String bankTranNo) {
        this.bankTranNo = bankTranNo;
    }
    
    /**
     * Check if payment was successful
     */
    public boolean isSuccess() {
        return valid && "00".equals(responseCode);
    }
}
