package SpringBoot.demo.Service.Order;

import SpringBoot.demo.Model.ShippingZone;
import SpringBoot.demo.Model.Voucher;
import SpringBoot.demo.Repository.ShippingZoneRepository;
import SpringBoot.demo.Repository.VoucherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service xử lý tính toán giá: tax, shipping fee, discount
 * Trách nhiệm: Tính toán giá cả, voucher, phí ship
 */
@Slf4j
@Service
public class OrderPricingService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private ShippingZoneRepository shippingZoneRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% VAT

    /**
     * Tính thuế
     */
    public BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(0, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Tính phí ship theo khu vực
     */
    public ShippingZoneResult getShippingFee(String district, String city) {
        try {
            log.info("Getting shipping fee for: {} - {}", district, city);

            Optional<ShippingZone> result = shippingZoneRepository.findByDistrictAndCity(district, city);

            if (result.isEmpty()) {
                log.warn("Shipping zone not found for: {} - {}", district, city);
                return new ShippingZoneResult(false, "Khu vực giao hàng không được hỗ trợ", BigDecimal.ZERO);
            }

            ShippingZone shippingZone = result.get();
            BigDecimal shippingFee = shippingZone.getShippingFee();
            log.info("Shipping fee found: {} VND", shippingFee);

            return new ShippingZoneResult(true, "Thành công", shippingFee);

        } catch (Exception e) {
            log.error("Error getting shipping fee: {}", e.getMessage(), e);
            return new ShippingZoneResult(false, "Lỗi tính phí ship: " + e.getMessage(), BigDecimal.ZERO);
        }
    }

    /**
     * Validate và áp dụng voucher
     */
    public VoucherValidationResult validateAndApplyVoucher(String voucherCode, BigDecimal subtotal) {
        try {
            log.info("Validating voucher: {}", voucherCode);

            Optional<Voucher> voucherOpt = voucherRepository.findActiveVoucherByCode(voucherCode, LocalDateTime.now());

            if (voucherOpt.isEmpty()) {
                log.warn("Voucher not found or not active: {}", voucherCode);
                return new VoucherValidationResult(false, "Mã giảm giá không hợp lệ", BigDecimal.ZERO);
            }

            Voucher voucher = voucherOpt.get();

            // Kiểm tra minimum order amount
            if (voucher.getMinOrderAmount() != null && subtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
                log.warn("Subtotal {} is less than minimum: {}", subtotal, voucher.getMinOrderAmount());
                return new VoucherValidationResult(false, 
                    "Đơn hàng phải từ " + voucher.getMinOrderAmount() + " VND trở lên", BigDecimal.ZERO);
            }

            // Tính discount
            BigDecimal discount = BigDecimal.ZERO;
            if (voucher.getDiscountValue() == null) {
                log.warn("Voucher discount value is null");
                return new VoucherValidationResult(false, "Mã giảm giá không hợp lệ", BigDecimal.ZERO);
            }
            
            if (voucher.getDiscountType().toString().equals("FIXED")) {
                discount = voucher.getDiscountValue();
            } else if (voucher.getDiscountType().toString().equals("PERCENT")) {
                discount = subtotal.multiply(voucher.getDiscountValue())
                        .divide(new BigDecimal(100), 0, java.math.RoundingMode.HALF_UP);
            }

            // Áp dụng max discount
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
                log.info("Discount capped to max: {}", discount);
            }

            log.info("Voucher validated successfully: code={}, discount={}", voucherCode, discount);
            return new VoucherValidationResult(true, "Thành công", discount);

        } catch (Exception e) {
            log.error("Error validating voucher: {}", e.getMessage(), e);
            return new VoucherValidationResult(false, "Lỗi xác thực voucher: " + e.getMessage(), BigDecimal.ZERO);
        }
    }

    /**
     * Tính tổng tiền
     */
    public BigDecimal calculateTotalAmount(BigDecimal subtotal, BigDecimal tax, BigDecimal shippingFee, BigDecimal discount) {
        return subtotal.add(tax).add(shippingFee).subtract(discount);
    }

    /**
     * Inner class: Kết quả validate voucher
     */
    public static class VoucherValidationResult {
        private boolean valid;
        private String message;
        private BigDecimal discount;

        public VoucherValidationResult(boolean valid, String message, BigDecimal discount) {
            this.valid = valid;
            this.message = message;
            this.discount = discount;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public BigDecimal getDiscount() {
            return discount;
        }
    }

    /**
     * Inner class: Kết quả shipping zone
     */
    public static class ShippingZoneResult {
        private boolean valid;
        private String message;
        private BigDecimal shippingFee;

        public ShippingZoneResult(boolean valid, String message, BigDecimal shippingFee) {
            this.valid = valid;
            this.message = message;
            this.shippingFee = shippingFee;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public BigDecimal getShippingFee() {
            return shippingFee;
        }
    }
}
