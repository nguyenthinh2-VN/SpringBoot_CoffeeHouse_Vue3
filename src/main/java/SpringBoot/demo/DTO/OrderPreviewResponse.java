package SpringBoot.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response preview giá đơn hàng
 * SRP: Chỉ chứa dữ liệu giá để FE hiển thị
 * 
 * Không tạo đơn, chỉ tính giá để user xem trước
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreviewResponse {
    private BigDecimal subtotal;                    // Tổng tiền hàng
    private BigDecimal tax;                         // Thuế (0% user, 10% staff)
    private BigDecimal shippingFee;                 // Phí vận chuyển
    private BigDecimal discount;                    // Giảm giá từ voucher
    private String voucherCode;                     // Mã voucher áp dụng
    private BigDecimal totalAmount;                 // Tổng cộng
    private List<OrderItemResponse> items;          // Chi tiết sản phẩm
    private String message;                         // Thông báo (e.g., "Giá có thể thay đổi")
}
