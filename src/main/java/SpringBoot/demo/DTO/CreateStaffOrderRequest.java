package SpringBoot.demo.DTO;

import SpringBoot.demo.Enum.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request tạo đơn hàng từ staff/admin tại quán
 * SRP: Chỉ chứa dữ liệu request cho staff order
 * 
 * Khác với user order:
 * - Không cần address (tại quán)
 * - Không có voucher
 * - Không có deliveryMethod (mặc định PICKUP)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStaffOrderRequest {
    private List<OrderItemRequest> items;           // Danh sách sản phẩm
    private PaymentMethod paymentMethod;            // Phương thức thanh toán
    private String notes;                           // Ghi chú
    private Long createdByStaffId;                  // ID staff tạo đơn (audit)
}
