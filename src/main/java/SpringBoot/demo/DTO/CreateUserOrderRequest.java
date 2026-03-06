package SpringBoot.demo.DTO;

import SpringBoot.demo.Enum.DeliveryMethod;
import SpringBoot.demo.Enum.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request tạo đơn hàng từ khách online
 * SRP: Chỉ chứa dữ liệu request cho user order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserOrderRequest {
    private List<OrderItemRequest> items;           // Danh sách sản phẩm
    private Long addressId;                         // Địa chỉ giao hàng (bắt buộc)
    private String voucherCode;                     // Mã voucher (tùy chọn)
    private PaymentMethod paymentMethod;            // Phương thức thanh toán
    private DeliveryMethod deliveryMethod;          // Phương thức giao hàng
    private String notes;                           // Ghi chú
}
