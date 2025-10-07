package SpringBoot.demo.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;

    // Constructors
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
