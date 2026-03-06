package SpringBoot.demo.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Boolean isUsed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PasswordResetToken() {
        this.createdAt = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusMinutes(10); // OTP hết hạn sau 10 phút
    }

    public PasswordResetToken(String email, String otp) {
        this();
        this.email = email;
        this.otp = otp;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
