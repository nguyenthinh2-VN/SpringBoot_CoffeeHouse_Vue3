package SpringBoot.demo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "vouchers")
@AllArgsConstructor
@NoArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Mã voucher không được để trống")
    @Column(unique = true, nullable = false)
    private String code;

    @NotNull(message = "Loại discount không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // FIXED hoặc PERCENT

    @NotNull(message = "Giá trị discount không được để trống")
    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount; // Tối thiểu để áp dụng

    @Column(name = "max_discount")
    private BigDecimal maxDiscount; // Giảm tối đa (cho PERCENT)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit; // Số lần sử dụng tối đa (null = không giới hạn)

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0; // Số lần đã sử dụng

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    // Enum cho loại discount
    public enum DiscountType {
        FIXED,    // Giảm cố định (VD: 50,000 VND)
        PERCENT   // Giảm theo phần trăm (VD: 20%)
    }
}
