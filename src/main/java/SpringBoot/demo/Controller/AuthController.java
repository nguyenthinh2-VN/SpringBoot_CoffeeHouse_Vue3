package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.*;
import SpringBoot.demo.Security.JwtUtil;
import SpringBoot.demo.Service.Auth.AuthService;
import SpringBoot.demo.Service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs quản lý xác thực và phân quyền")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetService passwordResetService;

    // POST /auth/register - Đăng ký user mới
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản user mới với role USER")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username hoặc email đã tồn tại")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            ApiResponse<UserResponse> response = authService.register(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<UserResponse> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // POST /auth/login - Đăng nhập
    @Operation(summary = "Đăng nhập tài khoản")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            AuthResponse errorResponse = AuthResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // POST /auth/refresh - Làm mới access token
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.AuthData>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            ApiResponse<AuthResponse.AuthData> response = authService.refreshToken(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<AuthResponse.AuthData> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /auth/profile - Lấy thông tin user (Protected)
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        try {
            // Lấy username từ Security Context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            ApiResponse<UserResponse> response = authService.getProfile(username);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<UserResponse> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // POST /auth/logout - Đăng xuất (Protected)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from Authorization header
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            if (token == null) {
                ApiResponse<String> errorResponse = ApiResponse.error("Token không được cung cấp");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            ApiResponse<String> response = authService.logout(token);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /auth/validate - Validate token (Utility endpoint)
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            if (token == null) {
                ApiResponse<String> errorResponse = ApiResponse.error("Token không được cung cấp");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            JwtUtil.TokenValidationResult result = jwtUtil.validateTokenWithDetails(token);
            
            if (result.isValid()) {
                String username = jwtUtil.extractUsername(token);
                Long remainingTime = jwtUtil.getTokenRemainingTime(token);
                
                ApiResponse<String> response = ApiResponse.success(
                    "Token hợp lệ. User: " + username + ". Còn lại: " + remainingTime + " giây"
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> errorResponse = ApiResponse.error(result.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // POST /auth/forgot-password - Gửi mã OTP qua email
    @Operation(summary = "Quên mật khẩu - Gửi OTP", description = "Gửi mã OTP đến email để đặt lại mật khẩu")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gửi OTP thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Email không tồn tại")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            ApiResponse<String> response = passwordResetService.sendOtp(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // POST /auth/verify-otp - Xác minh mã OTP
    @Operation(summary = "Xác minh mã OTP", description = "Kiểm tra mã OTP có hợp lệ không")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "OTP không hợp lệ hoặc đã hết hạn")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            ApiResponse<String> response = passwordResetService.verifyOtp(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // POST /auth/reset-password - Đặt lại mật khẩu
    @Operation(summary = "Đặt lại mật khẩu", description = "Đặt lại mật khẩu mới sau khi xác minh OTP")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "OTP không hợp lệ hoặc đã hết hạn")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            ApiResponse<String> response = passwordResetService.resetPassword(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
