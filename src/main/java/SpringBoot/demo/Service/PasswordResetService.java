package SpringBoot.demo.Service;

import SpringBoot.demo.DTO.*;
import SpringBoot.demo.Model.PasswordResetToken;
import SpringBoot.demo.Model.User;
import SpringBoot.demo.Repository.PasswordResetTokenRepository;
import SpringBoot.demo.Repository.User.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final SecureRandom random = new SecureRandom();

    /**
     * Gửi mã OTP đến email
     */
    @Transactional
    public ApiResponse<String> sendOtp(ForgotPasswordRequest request) {
        try {
            // Kiểm tra email có tồn tại không
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ApiResponse.error("Email không tồn tại trong hệ thống");
            }

            User user = userOpt.get();
            if (!user.getIsActive()) {
                return ApiResponse.error("Tài khoản đã bị khóa");
            }

            // Xóa các token cũ của email này
            tokenRepository.deleteByEmail(request.getEmail());

            // Tạo mã OTP 6 số
            String otp = generateOtp();

            // Lưu token vào database
            PasswordResetToken token = new PasswordResetToken(request.getEmail(), otp);
            tokenRepository.save(token);

            // Gửi email
            emailService.sendOtpEmail(request.getEmail(), otp);

            return ApiResponse.success("Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.");

        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi gửi OTP: " + e.getMessage());
        }
    }

    /**
     * Xác minh mã OTP
     */
    public ApiResponse<String> verifyOtp(VerifyOtpRequest request) {
        try {
            // Tìm token
            Optional<PasswordResetToken> tokenOpt = tokenRepository
                    .findByEmailAndOtpAndIsUsedFalse(request.getEmail(), request.getOtp());

            if (tokenOpt.isEmpty()) {
                return ApiResponse.error("Mã OTP không hợp lệ hoặc đã được sử dụng");
            }

            PasswordResetToken token = tokenOpt.get();

            // Kiểm tra hết hạn
            if (token.isExpired()) {
                return ApiResponse.error("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
            }

            return ApiResponse.success("Mã OTP hợp lệ. Bạn có thể đặt lại mật khẩu.");

        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi xác minh OTP: " + e.getMessage());
        }
    }

    /**
     * Đặt lại mật khẩu
     */
    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        try {
            // Tìm token
            Optional<PasswordResetToken> tokenOpt = tokenRepository
                    .findByEmailAndOtpAndIsUsedFalse(request.getEmail(), request.getOtp());

            if (tokenOpt.isEmpty()) {
                return ApiResponse.error("Mã OTP không hợp lệ hoặc đã được sử dụng");
            }

            PasswordResetToken token = tokenOpt.get();

            // Kiểm tra hết hạn
            if (token.isExpired()) {
                return ApiResponse.error("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
            }

            // Tìm user
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ApiResponse.error("Không tìm thấy người dùng");
            }

            User user = userOpt.get();

            // Cập nhật mật khẩu mới
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Đánh dấu token đã sử dụng
            token.setIsUsed(true);
            tokenRepository.save(token);

            return ApiResponse.success("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.");

        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi đặt lại mật khẩu: " + e.getMessage());
        }
    }

    /**
     * Xóa các token đã hết hạn (có thể chạy định kỳ)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    /**
     * Tạo mã OTP 6 số ngẫu nhiên
     */
    private String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
