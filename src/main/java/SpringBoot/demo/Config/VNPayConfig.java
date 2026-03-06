package SpringBoot.demo.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * VNPAY Configuration
 * Loads VNPAY credentials from environment variables
 */
@Component
public class VNPayConfig {
    
    @Value("${vnpay.tmnCode:}")
    private String tmnCode;
    
    @Value("${vnpay.hashSecret:}")
    private String hashSecret;
    
    @Value("${vnpay.apiUrl:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String apiUrl;
    
    @Value("${vnpay.returnUrl:}")
    private String returnUrl;
    
    @Value("${vnpay.version:2.1.0}")
    private String version;
    
    @Value("${vnpay.command:pay}")
    private String command;
    
    @Value("${vnpay.orderType:other}")
    private String orderType;
    
    // Getters
    public String getTmnCode() {
        return tmnCode;
    }
    
    public String getHashSecret() {
        return hashSecret;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getCommand() {
        return command;
    }
    
    public String getOrderType() {
        return orderType;
    }
    
    /**
     * Validate configuration
     */
    public boolean isConfigured() {
        return tmnCode != null && !tmnCode.isEmpty() 
            && hashSecret != null && !hashSecret.isEmpty()
            && returnUrl != null && !returnUrl.isEmpty();
    }
}
