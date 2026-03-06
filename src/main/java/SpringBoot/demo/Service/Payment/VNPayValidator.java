package SpringBoot.demo.Service.Payment;

import SpringBoot.demo.Config.VNPayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * VNPAY Signature Validator
 * Responsible for verifying HMAC SHA512 signatures from VNPAY
 */
@Component
public class VNPayValidator {
    
    private static final Logger log = LoggerFactory.getLogger(VNPayValidator.class);
    
    private final VNPayConfig vnPayConfig;
    
    public VNPayValidator(VNPayConfig vnPayConfig) {
        this.vnPayConfig = vnPayConfig;
    }
    
    /**
     * Validate VNPAY callback signature
     * @param params All parameters from VNPAY callback
     * @param secureHash The secure hash from VNPAY
     * @return true if signature is valid
     */
    public boolean validateSignature(Map<String, String> params, String secureHash) {
        try {
            log.info("=== VNPAY Signature Validation ===");
            log.info("Hash Secret: {}", vnPayConfig.getHashSecret());
            
            String signData = buildSignData(params);
            log.info("Sign Data: {}", signData);
            
            String computedHash = hmacSHA512(vnPayConfig.getHashSecret(), signData);
            log.info("Computed Hash: {}", computedHash);
            log.info("Received Hash: {}", secureHash);
            
            boolean isValid = computedHash.equals(secureHash);
            log.info("Signature Valid: {}", isValid);
            log.info("=== END VALIDATION ===");
            
            return isValid;
        } catch (Exception e) {
            log.error("Error validating signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Build signature data from parameters
     * Parameters must be sorted alphabetically and URL encoded
     */
    private String buildSignData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    // URL encode both key and value
                    String encodedKey = java.net.URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString());
                    String encodedValue = java.net.URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());
                    sb.append(encodedKey).append("=").append(encodedValue);
                } catch (UnsupportedEncodingException e) {
                    log.error("Error encoding parameter: {}", fieldName, e);
                    sb.append(fieldName).append("=").append(fieldValue);
                }
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * Generate HMAC SHA512 hash
     */
    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            key.getBytes(StandardCharsets.UTF_8),
            "HmacSHA512"
        );
        hmac.init(secretKeySpec);
        byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return toHexString(hash);
    }
    
    /**
     * Convert byte array to hex string
     */
    private String toHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
