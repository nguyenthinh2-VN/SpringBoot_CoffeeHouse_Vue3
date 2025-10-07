package SpringBoot.demo.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username hoặc email không được để trống")
    private String username; // Có thể là username hoặc email

    @NotBlank(message = "Password không được để trống")
    private String password;

    // Constructors
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
