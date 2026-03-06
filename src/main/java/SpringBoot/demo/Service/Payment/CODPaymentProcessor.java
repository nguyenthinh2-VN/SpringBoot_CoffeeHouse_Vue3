package SpringBoot.demo.Service.Payment;

import SpringBoot.demo.Enum.OrderStatus;
import SpringBoot.demo.Enum.PaymentStatus;
import SpringBoot.demo.Model.Order;
import SpringBoot.demo.Repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * COD (Cash On Delivery) Payment Processor
 * Handles COD payment confirmation
 * Single Responsibility: Process COD payments
 */
@Component
public class CODPaymentProcessor implements PaymentProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(CODPaymentProcessor.class);
    
    private final OrderRepository orderRepository;
    
    public CODPaymentProcessor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    public PaymentResult processPayment(Order order) {
        try {
            log.info("Processing COD payment for order: {}", order.getId());
            
            // COD doesn't require external payment processing
            // Just return success message
            PaymentResult result = new PaymentResult(true, "COD payment method selected");
            result.setTransactionId(order.getOrderCode());
            
            return result;
        } catch (Exception e) {
            log.error("Error processing COD payment: {}", e.getMessage(), e);
            return new PaymentResult(false, "Error processing COD payment: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public PaymentCallbackResult handleCallback(Map<String, String> params) {
        try {
            String orderCode = params.get("orderCode");
            log.info("Handling COD payment confirmation for order: {}", orderCode);
            
            // Find order by order code
            Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
            if (order == null) {
                log.warn("Order not found: {}", orderCode);
                return new PaymentCallbackResult(false, "Order not found", orderCode);
            }
            
            // Confirm COD payment
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            
            log.info("COD payment confirmed for order: {}", orderCode);
            
            PaymentCallbackResult result = new PaymentCallbackResult(true, "COD payment confirmed", orderCode);
            result.setPaymentStatus("PAID");
            result.setTransactionId(orderCode);
            result.setAmount(order.getTotalAmount().longValue());
            
            return result;
        } catch (Exception e) {
            log.error("Error handling COD payment confirmation: {}", e.getMessage(), e);
            return new PaymentCallbackResult(false, "Error confirming COD payment: " + e.getMessage(), null);
        }
    }
    
    @Override
    public String getPaymentMethod() {
        return "COD";
    }
}
