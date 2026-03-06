package SpringBoot.demo.Service.Payment;

import SpringBoot.demo.Config.VNPayConfig;
import SpringBoot.demo.DTO.Payment.VNPayPaymentRequest;
import SpringBoot.demo.DTO.Payment.VNPayPaymentResponse;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPAY Payment Service
 * Responsible for building payment requests and parsing payment responses
 * Single Responsibility: Handle VNPAY payment operations
 */
@Service
public class VNPayService {
    
    private final VNPayConfig vnPayConfig;
    private final VNPayValidator vnPayValidator;
    
    public VNPayService(VNPayConfig vnPayConfig, VNPayValidator vnPayValidator) {
        this.vnPayConfig = vnPayConfig;
        this.vnPayValidator = vnPayValidator;
    }
    
    /**
     * Build VNPAY payment URL
     * @param request Payment request with order details
     * @return Payment URL to redirect user to VNPAY
     */
    public String buildPaymentUrl(VNPayPaymentRequest request) throws UnsupportedEncodingException {
        if (!vnPayConfig.isConfigured()) {
            throw new IllegalStateException("VNPAY is not configured");
        }
        
        Map<String, String> vnpParams = new TreeMap<>();
        
        // Add VNPAY required parameters
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(request.getAmount())); // Amount in VND (multiply by 100)
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", request.getOrderCode()); // Order code
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + request.getOrderCode());
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", request.getIpAddress());
        vnpParams.put("vnp_CreateDate", getCurrentDateTime());
        
        // Build query string
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = vnpParams.keySet().iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            String value = vnpParams.get(key);
            query.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()))
                .append("=")
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
            if (itr.hasNext()) {
                query.append("&");
            }
        }
        
        // Generate secure hash
        String secureHash = generateSecureHash(query.toString());
        
        return vnPayConfig.getApiUrl() + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;
    }
    
    /**
     * Parse VNPAY callback response
     * @param params All parameters from VNPAY callback
     * @return Payment response with status and details
     */
    public VNPayPaymentResponse parseCallback(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        
        // Validate signature
        boolean isValid = vnPayValidator.validateSignature(params, secureHash);
        
        VNPayPaymentResponse response = new VNPayPaymentResponse();
        response.setValid(isValid);
        response.setOrderCode(params.get("vnp_TxnRef"));
        response.setTransactionId(params.get("vnp_TransactionNo"));
        response.setResponseCode(params.get("vnp_ResponseCode"));
        response.setMessage(getResponseMessage(params.get("vnp_ResponseCode")));
        response.setAmount(Long.parseLong(params.getOrDefault("vnp_Amount", "0")) / 100); // Convert back to VND
        response.setPayDate(params.get("vnp_PayDate"));
        response.setBankCode(params.get("vnp_BankCode"));
        response.setBankTranNo(params.get("vnp_BankTranNo"));
        
        return response;
    }
    
    /**
     * Generate HMAC SHA512 secure hash
     */
    private String generateSecureHash(String data) throws UnsupportedEncodingException {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                vnPayConfig.getHashSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA512"
            );
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating secure hash", e);
        }
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
    
    /**
     * Get current datetime in VNPAY format (yyyyMMddHHmmss)
     */
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }
    
    /**
     * Get human-readable response message
     */
    private String getResponseMessage(String responseCode) {
        return switch (responseCode) {
            case "00" -> "Giao dịch thành công";
            case "01" -> "Giao dịch bị từ chối";
            case "02" -> "Merchant đóng kết nối trước khi nhận được kết quả từ VNPAY";
            case "04" -> "Giao dịch không được tìm thấy";
            case "05" -> "Giao dịch đã được xác nhận trước đó";
            case "06" -> "Yêu cầu giao dịch bị lỗi";
            case "07" -> "Merchant không được phép thực hiện loại giao dịch này";
            case "09" -> "Giao dịch bị từ chối do nghi ngờ gian lận";
            default -> "Lỗi không xác định";
        };
    }
}
