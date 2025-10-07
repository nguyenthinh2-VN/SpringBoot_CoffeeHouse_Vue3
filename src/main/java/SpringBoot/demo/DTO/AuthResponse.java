package SpringBoot.demo.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthResponse {

    private boolean success;
    private String message;
    private AuthData data;

    // Constructors
    public AuthResponse(boolean success, String message, AuthData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Static methods for convenience
    public static AuthResponse success(String message, AuthData data) {
        return new AuthResponse(true, message, data);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message, null);
    }

    @Getter
    @Setter
    public static class AuthData {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresIn; // seconds
        private UserResponse user;

        // Constructors
        public AuthData() {
        }

        public AuthData(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.user = user;
        }
    }
}
