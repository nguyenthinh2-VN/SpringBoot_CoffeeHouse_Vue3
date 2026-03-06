package SpringBoot.demo.Repository;

import SpringBoot.demo.Model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    // Lấy tất cả địa chỉ của user
    List<Address> findByUserId(Long userId);

    // Lấy địa chỉ mặc định của user
    @Query("SELECT a FROM Address a WHERE a.userId = :userId AND a.isDefault = true")
    Optional<Address> findDefaultByUserId(@Param("userId") Long userId);

    // Kiểm tra địa chỉ có thuộc user không
    @Query("SELECT COUNT(a) > 0 FROM Address a WHERE a.id = :addressId AND a.userId = :userId")
    boolean existsByIdAndUserId(@Param("addressId") Integer addressId, @Param("userId") Long userId);

    // Lấy địa chỉ theo ID và user
    @Query("SELECT a FROM Address a WHERE a.id = :addressId AND a.userId = :userId")
    Optional<Address> findByIdAndUserId(@Param("addressId") Integer addressId, @Param("userId") Long userId);
}
