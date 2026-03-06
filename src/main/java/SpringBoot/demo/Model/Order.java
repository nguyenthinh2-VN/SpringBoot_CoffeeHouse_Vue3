package SpringBoot.demo.Model;

import SpringBoot.demo.Enum.DeliveryMethod;
import SpringBoot.demo.Enum.OrderStatus;
import SpringBoot.demo.Enum.PaymentMethod;
import SpringBoot.demo.Enum.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = true) // Nullable cho staff order
    private Long userId;

    @NotNull(message = "Mã đơn hàng không được để trống")
    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode;

    @NotNull(message = "Tổng tiền không được để trống")
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "tax")
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount")
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "voucher_code")
    private String voucherCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false)
    private DeliveryMethod deliveryMethod;

    @Column(name = "address_id")
    private Integer addressId; // FK đến bảng addresses

    @Column(name = "created_by_staff_id")
    private Long createdByStaffId; // Staff ID tạo đơn (audit, chỉ cho staff order)

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderStatusLog> statusLogs;

    // Constructors
    public Order(Long userId, String orderCode, BigDecimal totalAmount, 
                 PaymentMethod paymentMethod, DeliveryMethod deliveryMethod) {
        this.userId = userId;
        this.orderCode = orderCode;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.deliveryMethod = deliveryMethod;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
