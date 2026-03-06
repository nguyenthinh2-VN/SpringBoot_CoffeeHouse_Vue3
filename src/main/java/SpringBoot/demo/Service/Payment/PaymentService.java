package SpringBoot.demo.Service.Payment;

import SpringBoot.demo.Enum.PaymentMethod;
import SpringBoot.demo.Model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Payment Service
 * Orchestrates payment processing using strategy pattern
 * Single Responsibility: Coordinate payment processing
 */
@Service
public class PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentProcessorFactory processorFactory;
    
    public PaymentService(PaymentProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }
    
    /**
     * Process payment for an order
     * @param order The order to process payment for
     * @return Payment result
     */
    public PaymentResult processPayment(Order order) {
        try {
            log.info("Processing payment for order: {} with method: {}", 
                order.getId(), order.getPaymentMethod());
            
            PaymentProcessor processor = processorFactory.getProcessor(order.getPaymentMethod());
            return processor.processPayment(order);
        } catch (Exception e) {
            log.error("Error processing payment for order: {}", order.getId(), e);
            return new PaymentResult(false, "Error processing payment: " + e.getMessage());
        }
    }
    
    /**
     * Handle payment callback
     * @param paymentMethod Payment method
     * @param params Callback parameters
     * @return Callback result
     */
    public PaymentCallbackResult handleCallback(PaymentMethod paymentMethod, Map<String, String> params) {
        try {
            log.info("Handling payment callback for method: {}", paymentMethod);
            
            PaymentProcessor processor = processorFactory.getProcessor(paymentMethod);
            return processor.handleCallback(params);
        } catch (Exception e) {
            log.error("Error handling payment callback: {}", e.getMessage(), e);
            return new PaymentCallbackResult(false, "Error handling callback: " + e.getMessage(), null);
        }
    }
}
