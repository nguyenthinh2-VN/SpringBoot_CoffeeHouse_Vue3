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
@Table(name = "shipping_zones")
@AllArgsConstructor
@NoArgsConstructor
public class ShippingZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên khu vực không được để trống")
    @Column(name = "zone_name", nullable = false)
    private String zoneName; // Ví dụ: "Quận 1", "Quận 2"

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Column(name = "district", nullable = false)
    private String district; // Ví dụ: "Quận 1", "Huyện Bình Chánh"

    @NotBlank(message = "Thành phố không được để trống")
    @Column(name = "city", nullable = false)
    private String city; // Ví dụ: "TP.HCM", "Hà Nội"

    @NotNull(message = "Phí ship không được để trống")
    @Column(name = "shipping_fee", nullable = false)
    private BigDecimal shippingFee; // Phí ship cho khu vực này

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
}
