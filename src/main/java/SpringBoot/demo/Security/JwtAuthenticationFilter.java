package SpringBoot.demo.Security;

import SpringBoot.demo.Service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        
        // Skip JWT processing cho public endpoints
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy Authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Kiểm tra header có Bearer token không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Nếu là protected endpoint mà không có token
            if (isProtectedEndpoint(requestURI)) {
                sendErrorResponse(response, 401, "MISSING_TOKEN", "Authorization header không được cung cấp hoặc không đúng format");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);

        try {
            // Validate token với details
            JwtUtil.TokenValidationResult validationResult = jwtUtil.validateTokenWithDetails(jwt);
            
            if (!validationResult.isValid()) {
                sendErrorResponse(response, 401, validationResult.getErrorCode(), validationResult.getMessage());
                return;
            }

            // Kiểm tra token type (phải là access token)
            if (!jwtUtil.isAccessToken(jwt)) {
                sendErrorResponse(response, 401, "WRONG_TOKEN_TYPE", "Phải sử dụng access token, không phải refresh token");
                return;
            }

            username = jwtUtil.extractUsername(jwt);

            // Nếu username hợp lệ và chưa có authentication trong context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                try {
                    // Load user details
                    UserDetails userDetails = authService.loadUserByUsername(username);

                    // Validate token với user details
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        
                        // Tạo authentication token
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        
                        // Set details
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set authentication vào Security Context
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        sendErrorResponse(response, 401, "TOKEN_USER_MISMATCH", "Token không khớp với user hiện tại");
                        return;
                    }
                } catch (Exception userEx) {
                    sendErrorResponse(response, 401, "USER_NOT_FOUND", "User trong token không tồn tại: " + userEx.getMessage());
                    return;
                }
            }
        } catch (Exception e) {
            // Log error và trả về lỗi cụ thể
            logger.error("JWT Authentication error: {}" + e.getMessage());
            sendErrorResponse(response, 401, "TOKEN_PROCESSING_ERROR", "Lỗi xử lý token: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Kiểm tra endpoint có cần authentication không
    private boolean isProtectedEndpoint(String uri) {
        return uri.startsWith("/auth/profile") || 
               uri.startsWith("/auth/logout") ||
               uri.startsWith("/auth/validate");
    }

    // Kiểm tra endpoint có phải public không (không cần JWT processing)
    private boolean isPublicEndpoint(String uri) {
        return uri.startsWith("/auth/register") ||
               uri.startsWith("/auth/login") ||
               uri.startsWith("/auth/refresh") ||
               uri.startsWith("/products");
    }

    // Gửi error response với format JSON
    private void sendErrorResponse(HttpServletResponse response, int status, String errorCode, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"success\": false, \"errorCode\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}", 
            errorCode, 
            message, 
            java.time.Instant.now().toString()
        );
        
        response.getWriter().write(jsonResponse);
    }
}
