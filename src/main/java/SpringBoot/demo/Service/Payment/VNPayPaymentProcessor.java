package SpringBoot.demo.Service.Payment;

import SpringBoot.demo.Enum.OrderStatus;
import SpringBoot.demo.Enum.PaymentStatus;
import SpringBoot.demo.Model.Order;
import SpringBoot.demo.Repository.OrderRepository;
import SpringBoot.demo.DTO.Payment.VNPayPaymentRequest;
import SpringBoot.demo.DTO.Payment.VNPayPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * VNPAY Payment Processor
 * Handles VNPAY payment processing and callbacks
 * Single Responsibility: Process VNPAY payments
 */
@Component
public class VNPayPaymentProcessor implements PaymentProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(VNPayPaymentProcessor.class);
    
    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;
    
    public VNPayPaymentProcessor(VNPayService vnPayService, OrderRepository orderRepository) {
        this.vnPayService = vnPayService;
        this.orderRepository = orderRepository;
    }
    
    @Override
    public PaymentResult processPayment(Order order) {
        try {
            log.info("Processing VNPAY payment for order: {}", order.getId());
            
            // Create VNPAY payment request
            VNPayPaymentRequest request = new VNPayPaymentRequest(
                order.getId(),
                order.getOrderCode(),
                order.getTotalAmount().longValue(),
                "127.0.0.1" // Should be client IP in real scenario
            );
            
            // Build payment URL
            String paymentUrl = vnPayService.buildPaymentUrl(request);
            
            log.info("VNPAY payment URL generated for order: {}", order.getOrderCode());
            
            PaymentResult result = new PaymentResult(true, "Payment link created successfully");
            result.setRedirectUrl(paymentUrl);
            result.setTransactionId(order.getOrderCode());
            
            return result;
        } catch (UnsupportedEncodingException e) {
            log.error("Error building VNPAY payment URL: {}", e.getMessage(), e);
            return new PaymentResult(false, "Error creating payment link: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public PaymentCallbackResult handleCallback(Map<String, String> params) {
        try {
            log.info("Handling VNPAY callback");
            
            // Parse callback response
            VNPayPaymentResponse response = vnPayService.parseCallback(params);
            
            log.info("VNPAY callback parsed - OrderCode: {}, ResponseCode: {}, Valid: {}", 
                response.getOrderCode(), response.getResponseCode(), response.isValid());
            
            // Validate signature
            if (!response.isValid()) {
                log.warn("Invalid VNPAY signature for order: {}", response.getOrderCode());
                return new PaymentCallbackResult(false, "Invalid signature", response.getOrderCode());
            }
            
            // Find order by order code
            Order order = orderRepository.findByOrderCode(response.getOrderCode()).orElse(null);
            if (order == null) {
                log.warn("Order not found: {}", response.getOrderCode());
                return new PaymentCallbackResult(false, "Order not found", response.getOrderCode());
            }
            
            PaymentCallbackResult result = new PaymentCallbackResult(
                response.isSuccess(),
                response.getMessage(),
                response.getOrderCode()
            );
            result.setTransactionId(response.getTransactionId());
            result.setAmount(response.getAmount());
            
            // Update order based on payment result
            if (response.isSuccess()) {
                log.info("VNPAY payment successful for order: {}", order.getOrderCode());
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setStatus(OrderStatus.CONFIRMED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                result.setPaymentStatus("PAID");
                result.setSuccess(true);
            } else {
                log.warn("VNPAY payment failed for order: {}", order.getOrderCode());
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                result.setPaymentStatus("FAILED");
                result.setSuccess(false);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error handling VNPAY callback: {}", e.getMessage(), e);
            return new PaymentCallbackResult(false, "Error processing callback: " + e.getMessage(), null);
        }
    }
    
    @Override
    public String getPaymentMethod() {
        return "VNPAY";
    }
}
