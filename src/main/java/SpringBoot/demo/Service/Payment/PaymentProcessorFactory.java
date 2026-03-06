package SpringBoot.demo.Service.Payment;

import SpringBoot.demo.Enum.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Payment Processor Factory
 * Factory pattern to get appropriate payment processor
 * Single Responsibility: Create payment processor instances
 */
@Component
public class PaymentProcessorFactory {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorFactory.class);
    
    private final Map<String, PaymentProcessor> processors = new HashMap<>();
    
    public PaymentProcessorFactory(List<PaymentProcessor> paymentProcessors) {
        // Register all payment processors
        for (PaymentProcessor processor : paymentProcessors) {
            processors.put(processor.getPaymentMethod(), processor);
            log.info("Registered payment processor: {}", processor.getPaymentMethod());
        }
    }
    
    /**
     * Get payment processor by payment method
     * @param paymentMethod Payment method enum
     * @return PaymentProcessor instance
     * @throws IllegalArgumentException if processor not found
     */
    public PaymentProcessor getProcessor(PaymentMethod paymentMethod) {
        String method = paymentMethod.name();
        PaymentProcessor processor = processors.get(method);
        
        if (processor == null) {
            log.warn("Payment processor not found for method: {}", method);
            throw new IllegalArgumentException("Payment processor not found for method: " + method);
        }
        
        return processor;
    }
    
    /**
     * Get payment processor by string method name
     */
    public PaymentProcessor getProcessor(String paymentMethod) {
        PaymentProcessor processor = processors.get(paymentMethod);
        
        if (processor == null) {
            log.warn("Payment processor not found for method: {}", paymentMethod);
            throw new IllegalArgumentException("Payment processor not found for method: " + paymentMethod);
        }
        
        return processor;
    }
    
    /**
     * Check if processor exists for payment method
     */
    public boolean hasProcessor(PaymentMethod paymentMethod) {
        return processors.containsKey(paymentMethod.name());
    }
}
