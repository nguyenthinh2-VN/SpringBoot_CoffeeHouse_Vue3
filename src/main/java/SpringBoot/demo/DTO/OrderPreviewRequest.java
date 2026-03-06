package SpringBoot.demo.DTO;

import SpringBoot.demo.Enum.DeliveryMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request preview giá đơn hàng (chưa tạo đơn)
 * SRP: Chỉ chứa dữ liệu để tính giá preview
 * 
 * Dùng để FE hiển thị giá trước khi user đặt hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreviewRequest {
    private List<OrderItemRequest> items;           // Danh sách sản phẩm
    private Integer addressId;                         // Địa chỉ (nullable cho PICKUP)
    private String voucherCode;                     // Mã voucher (tùy chọn)
    private DeliveryMethod deliveryMethod;          // Phương thức giao hàng
}
