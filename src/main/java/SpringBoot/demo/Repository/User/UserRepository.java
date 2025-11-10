package SpringBoot.demo.Repository.User;

import SpringBoot.demo.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm user theo username
    Optional<User> findByUsername(String username);
    
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    
    // Tìm user theo username hoặc email (cho login)
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // Kiểm tra username đã tồn tại
    boolean existsByUsername(String username);
    
    // Kiểm tra email đã tồn tại
    boolean existsByEmail(String email);
    
    // Tìm user active theo username
    Optional<User> findByUsernameAndIsActive(String username, Boolean isActive);
}
