package SpringBoot.demo.DTO;

import SpringBoot.demo.Enum.DeliveryMethod;
import SpringBoot.demo.Enum.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Phương thức giao hàng không được để trống")
    private DeliveryMethod deliveryMethod;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    // Chỉ bắt buộc nếu deliveryMethod = DELIVERY
    private Integer addressId; // ID địa chỉ từ bảng addresses

    private String notes; // Ghi chú cho đơn hàng

    private String voucherCode; // Mã giảm giá (optional)
}
