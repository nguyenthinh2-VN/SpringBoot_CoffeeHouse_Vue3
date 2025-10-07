package SpringBoot.demo.DTO;

import SpringBoot.demo.Model.Role;
import SpringBoot.demo.Model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private Boolean isActive;

    // Constructors
    public UserResponse(Long id, String username, String email, Role role, LocalDateTime createdAt, Boolean isActive) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Static method để convert từ User entity
    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getIsActive()
        );
    }
}
