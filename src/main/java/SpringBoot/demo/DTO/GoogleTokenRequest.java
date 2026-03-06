package SpringBoot.demo.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenRequest {
    
    @NotBlank(message = "Google token không được để trống")
    private String token;
}
