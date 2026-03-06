package SpringBoot.demo.Service.Order.Pricing;

import SpringBoot.demo.Repository.ShippingZoneRepository;
import SpringBoot.demo.Repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Pricing strategy cho đơn hàng user (online)
 * SRP: Chỉ tính giá cho user order
 * 
 * Quy tắc:
 * - Tax: 0% (không có)
 * - ShippingFee: Tính theo khu vực
 * - Discount: Áp dụng voucher
 * - Total = Subtotal + ShippingFee - Discount
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserPricingStrategy implements PricingStrategy {
    
    private final ShippingZoneRepository shippingZoneRepository;
    private final VoucherRepository voucherRepository;
    
    @Override
    public BigDecimal calculateTax(BigDecimal subtotal) {
        log.info("User order: Tax = 0%");
        return BigDecimal.ZERO; // User order không có thuế
    }
    
    @Override
    public BigDecimal calculateShippingFee(String district, String city) {
        try {
            var shippingZone = shippingZoneRepository.findByDistrictAndCity(district, city);
            if (shippingZone.isEmpty()) {
                log.warn("Shipping zone not found: {} - {}", district, city);
                throw new RuntimeException("Khu vực giao hàng không được hỗ trợ");
            }
            BigDecimal fee = shippingZone.get().getShippingFee();
            log.info("Shipping fee calculated: {} VND for {} - {}", fee, district, city);
            return fee;
        } catch (Exception e) {
            log.error("Error calculating shipping fee: {}", e.getMessage());
            throw new RuntimeException("Lỗi tính phí vận chuyển: " + e.getMessage());
        }
    }
    
    @Override
    public BigDecimal calculateDiscount(String voucherCode, BigDecimal subtotal) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            log.info("No voucher code provided");
            return BigDecimal.ZERO;
        }
        
        try {
            var voucher = voucherRepository.findActiveVoucherByCode(voucherCode, LocalDateTime.now());
            if (voucher.isEmpty()) {
                log.warn("Voucher not found or not active: {}", voucherCode);
                throw new RuntimeException("Mã voucher không hợp lệ hoặc đã hết hạn");
            }
            
            var v = voucher.get();
            
            // Kiểm tra minimum order amount
            if (v.getMinOrderAmount() != null && subtotal.compareTo(v.getMinOrderAmount()) < 0) {
                log.warn("Subtotal {} < minimum {}", subtotal, v.getMinOrderAmount());
                throw new RuntimeException("Đơn hàng phải từ " + v.getMinOrderAmount() + " VND trở lên");
            }
            
            // Tính discount
            BigDecimal discount = BigDecimal.ZERO;
            if (v.getDiscountValue() == null) {
                log.warn("Voucher discount value is null");
                throw new RuntimeException("Mã voucher không hợp lệ");
            }
            
            if (v.getDiscountType().toString().equals("FIXED")) {
                discount = v.getDiscountValue();
            } else if (v.getDiscountType().toString().equals("PERCENT")) {
                discount = subtotal.multiply(v.getDiscountValue())
                        .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
            }
            
            // Áp dụng max discount
            if (v.getMaxDiscount() != null && discount.compareTo(v.getMaxDiscount()) > 0) {
                discount = v.getMaxDiscount();
                log.info("Discount capped to max: {}", discount);
            }
            
            log.info("Voucher applied: code={}, discount={}", voucherCode, discount);
            return discount;
        } catch (Exception e) {
            log.error("Error calculating discount: {}", e.getMessage());
            throw new RuntimeException("Lỗi xác thực voucher: " + e.getMessage());
        }
    }
    
    @Override
    public BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal tax, 
                                    BigDecimal shippingFee, BigDecimal discount) {
        BigDecimal total = subtotal.add(tax).add(shippingFee).subtract(discount);
        log.info("User order total: {} + {} + {} - {} = {}", 
                 subtotal, tax, shippingFee, discount, total);
        return total;
    }
}
