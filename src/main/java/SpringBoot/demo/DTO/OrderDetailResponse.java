package SpringBoot.demo.DTO;

import SpringBoot.demo.Enum.DeliveryMethod;
import SpringBoot.demo.Enum.OrderStatus;
import SpringBoot.demo.Enum.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    private Long id;

    private String orderCode;

    private Long userId;

    private BigDecimal totalAmount;

    private BigDecimal tax;

    private BigDecimal shippingFee;

    private BigDecimal discount;

    private String voucherCode;

    private OrderStatus status;

    private PaymentMethod paymentMethod;

    private DeliveryMethod deliveryMethod;

    private Integer addressId;

    private AddressResponse address;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<OrderItemResponse> items;

    private List<OrderStatusLogResponse> statusLogs;
}
