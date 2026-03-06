package SpringBoot.demo.Service.Order.Pricing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing strategy cho đơn hàng staff (tại quán)
 * SRP: Chỉ tính giá cho staff order
 * 
 * Quy tắc:
 * - Tax: 10% (có thuế VAT)
 * - ShippingFee: 0 (không có)
 * - Discount: 0 (không voucher)
 * - Total = Subtotal + Tax
 */
@Slf4j
@Component
public class StaffPricingStrategy implements PricingStrategy {
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    
    @Override
    public BigDecimal calculateTax(BigDecimal subtotal) {
        BigDecimal tax = subtotal.multiply(TAX_RATE)
                                 .setScale(0, RoundingMode.HALF_UP);
        log.info("Staff order: Tax = {} (10% of {})", tax, subtotal);
        return tax;
    }
    
    @Override
    public BigDecimal calculateShippingFee(String district, String city) {
        log.info("Staff order: Shipping fee = 0 (tại quán)");
        return BigDecimal.ZERO; // Staff order không có phí ship
    }
    
    @Override
    public BigDecimal calculateDiscount(String voucherCode, BigDecimal subtotal) {
        log.info("Staff order: Discount = 0 (không voucher)");
        return BigDecimal.ZERO; // Staff order không có voucher
    }
    
    @Override
    public BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal tax, 
                                    BigDecimal shippingFee, BigDecimal discount) {
        BigDecimal total = subtotal.add(tax);
        log.info("Staff order total: {} + {} = {}", subtotal, tax, total);
        return total;
    }
}
