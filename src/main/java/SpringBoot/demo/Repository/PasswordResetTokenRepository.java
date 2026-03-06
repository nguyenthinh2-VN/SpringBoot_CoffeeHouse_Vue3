package SpringBoot.demo.Repository;

import SpringBoot.demo.Model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByEmailAndOtpAndIsUsedFalse(String email, String otp);
    
    Optional<PasswordResetToken> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);
    
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
    
    void deleteByEmail(String email);
}
