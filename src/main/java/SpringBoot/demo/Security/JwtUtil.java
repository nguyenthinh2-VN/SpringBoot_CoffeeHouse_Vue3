package SpringBoot.demo.Security;

import SpringBoot.demo.Model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    // Tạo secret key từ string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Tạo Access Token
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "access");
        
        return createToken(claims, user.getUsername(), accessTokenExpiration);
    }

    // Tạo Refresh Token
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tokenType", "refresh");
        
        return createToken(claims, user.getUsername(), refreshTokenExpiration);
    }

    // Tạo token với claims và expiration
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy username từ token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Lấy userId từ token
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    // Lấy role từ token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Lấy token type từ token
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    // Lấy expiration date từ token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method để lấy claim từ token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Lấy tất cả claims từ token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token đã hết hạn");
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Token không được hỗ trợ");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Token không đúng định dạng");
        } catch (SignatureException e) {
            throw new RuntimeException("Chữ ký token không hợp lệ");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Token rỗng hoặc null");
        } catch (JwtException e) {
            throw new RuntimeException("Token không hợp lệ: " + e.getMessage());
        }
    }

    // Kiểm tra token có hết hạn không
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // Validate token với UserDetails
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Validate token (chỉ kiểm tra format và expiration)
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Kiểm tra token có phải là access token không
    public Boolean isAccessToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "access".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    // Kiểm tra token có phải là refresh token không
    public Boolean isRefreshToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    // Lấy thời gian còn lại của token (seconds)
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            return (expiration.getTime() - now.getTime()) / 1000;
        } catch (Exception e) {
            return 0L;
        }
    }

    // Validate token với error details
    public TokenValidationResult validateTokenWithDetails(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new TokenValidationResult(false, "TOKEN_MISSING", "Token không được cung cấp");
        }

        try {
            // Remove Bearer prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = extractAllClaims(token);
            
            if (isTokenExpired(token)) {
                return new TokenValidationResult(false, "TOKEN_EXPIRED", "Token đã hết hạn");
            }

            return new TokenValidationResult(true, "VALID", "Token hợp lệ");
            
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message.contains("hết hạn")) {
                return new TokenValidationResult(false, "TOKEN_EXPIRED", message);
            } else if (message.contains("không đúng định dạng")) {
                return new TokenValidationResult(false, "TOKEN_MALFORMED", message);
            } else if (message.contains("chữ ký")) {
                return new TokenValidationResult(false, "TOKEN_SIGNATURE_INVALID", message);
            } else if (message.contains("rỗng")) {
                return new TokenValidationResult(false, "TOKEN_EMPTY", message);
            } else {
                return new TokenValidationResult(false, "TOKEN_INVALID", message);
            }
        }
    }

    // Inner class cho kết quả validation
    public static class TokenValidationResult {
        private final boolean valid;
        private final String errorCode;
        private final String message;

        public TokenValidationResult(boolean valid, String errorCode, String message) {
            this.valid = valid;
            this.errorCode = errorCode;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
    }
}
