package SpringBoot.demo.Service.Order.Pricing;

import java.math.BigDecimal;

/**
 * Strategy interface cho tính giá
 * SRP: Định nghĩa contract cho các strategy tính giá khác nhau
 * 
 * Dễ mở rộng: Thêm loại order mới chỉ cần implement interface này
 */
public interface PricingStrategy {
    
    /**
     * Tính thuế
     */
    BigDecimal calculateTax(BigDecimal subtotal);
    
    /**
     * Tính phí vận chuyển
     */
    BigDecimal calculateShippingFee(String district, String city);
    
    /**
     * Tính giảm giá từ voucher
     */
    BigDecimal calculateDiscount(String voucherCode, BigDecimal subtotal);
    
    /**
     * Tính tổng tiền
     */
    BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal tax, 
                             BigDecimal shippingFee, BigDecimal discount);
}
