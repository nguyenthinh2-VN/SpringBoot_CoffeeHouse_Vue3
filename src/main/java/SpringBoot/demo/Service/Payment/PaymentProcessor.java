package SpringBoot.demo.Service.Payment;

import SpringBoot.demo.Model.Order;

import java.util.Map;

/**
 * Payment Processor Interface
 * Strategy pattern for different payment methods
 * Single Responsibility: Define contract for payment processing
 */
public interface PaymentProcessor {
    
    /**
     * Process payment for an order
     * @param order The order to process payment for
     * @return Payment result (URL for redirect, confirmation, etc.)
     */
    PaymentResult processPayment(Order order);
    
    /**
     * Handle payment callback/confirmation
     * @param params Callback parameters from payment gateway
     * @return Callback result with payment status
     */
    PaymentCallbackResult handleCallback(Map<String, String> params);
    
    /**
     * Get payment method this processor handles
     */
    String getPaymentMethod();
}
