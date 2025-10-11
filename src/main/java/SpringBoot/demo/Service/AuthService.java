package SpringBoot.demo.Service;

import SpringBoot.demo.DTO.*;
import SpringBoot.demo.Model.Role;
import SpringBoot.demo.Model.User;
import SpringBoot.demo.Repository.UserRepository;
import SpringBoot.demo.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    // UserDetailsService implementation for Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsernameOrEmail(username, username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User không tồn tại: " + username);
        }
        return user.get();
    }

    // Đăng ký user mới
    public ApiResponse<UserResponse> register(RegisterRequest request) {
        try {
            // Validate password matching
            if (!request.isPasswordMatching()) {
                return ApiResponse.error("Password và confirm password không khớp");
            }

            // Check username exists
            if (userRepository.existsByUsername(request.getUsername())) {
                return ApiResponse.error("Username đã tồn tại");
            }

            // Check email exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return ApiResponse.error("Email đã tồn tại");
            }

            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.USER);
            user.setIsActive(true);

            // Save user
            User savedUser = userRepository.save(user);

            // Convert to response DTO
            UserResponse userResponse = UserResponse.fromUser(savedUser);

            return ApiResponse.success("Đăng ký thành công", userResponse);

        } catch (Exception e) {
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Đăng nhập
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = (User) authentication.getPrincipal();

            // Check if user is active
            if (!user.getIsActive()) {
                return AuthResponse.error("Tài khoản đã bị khóa");
            }

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            // Create response data
            UserResponse userResponse = UserResponse.fromUser(user);
            AuthResponse.AuthData authData = new AuthResponse.AuthData(
                    accessToken,
                    refreshToken,
                    1800, // 30 minutes in seconds
                    userResponse
            );

            return AuthResponse.success("Đăng nhập thành công", authData);

        } catch (BadCredentialsException e) {
            return AuthResponse.error("Username hoặc password không đúng");
        } catch (Exception e) {
            return AuthResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Refresh access token
    public ApiResponse<AuthResponse.AuthData> refreshToken(RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken)) {
                return ApiResponse.error("Refresh token không hợp lệ");
            }

            // Check if it's actually a refresh token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                return ApiResponse.error("Token type không hợp lệ");
            }

            // Get username from token
            String username = jwtUtil.extractUsername(refreshToken);
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                return ApiResponse.error("User không tồn tại");
            }

            User user = userOpt.get();

            // Check if user is still active
            if (!user.getIsActive()) {
                return ApiResponse.error("Tài khoản đã bị khóa");
            }

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(user);

            // Create response data (only return new access token)
            AuthResponse.AuthData authData = new AuthResponse.AuthData();
            authData.setAccessToken(newAccessToken);
            authData.setExpiresIn(1800); // 30 minutes

            return ApiResponse.success("Token đã được làm mới", authData);

        } catch (Exception e) {
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Get user profile
    public ApiResponse<UserResponse> getProfile(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                return ApiResponse.error("User không tồn tại");
            }

            User user = userOpt.get();
            UserResponse userResponse = UserResponse.fromUser(user);

            return ApiResponse.success("Lấy thông tin thành công", userResponse);

        } catch (Exception e) {
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Logout (for future token blacklisting)
    public ApiResponse<String> logout(String token) {
        try {
            // TODO: Implement token blacklisting
            // For now, just return success
            // In production, you should add token to blacklist table

            return ApiResponse.success("Đăng xuất thành công");

        } catch (Exception e) {
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Validate user exists and is active
    public boolean isUserValid(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && user.get().getIsActive();
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
