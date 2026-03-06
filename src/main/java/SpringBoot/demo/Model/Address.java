package SpringBoot.demo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "addresses")
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "User ID không được để trống")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Tên người nhận không được để trống")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Column(name = "district", nullable = false)
    private String district;

    @NotBlank(message = "Thành phố không được để trống")
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Column(name = "full_address", columnDefinition = "TEXT", nullable = false)
    private String fullAddress;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

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
