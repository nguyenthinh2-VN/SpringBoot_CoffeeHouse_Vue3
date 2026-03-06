package SpringBoot.demo.Repository;

import SpringBoot.demo.Model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    // Tìm voucher theo code
    Optional<Voucher> findByCode(String code);

    // Tìm voucher còn hoạt động theo code
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true " +
           "AND (v.startDate IS NULL OR v.startDate <= :now) " +
           "AND (v.endDate IS NULL OR v.endDate >= :now)")
    Optional<Voucher> findActiveVoucherByCode(@Param("code") String code, @Param("now") LocalDateTime now);

    // Kiểm tra voucher có còn sử dụng được không
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true " +
           "AND (v.startDate IS NULL OR v.startDate <= :now) " +
           "AND (v.endDate IS NULL OR v.endDate >= :now) " +
           "AND (v.usageLimit IS NULL OR v.usageCount < v.usageLimit)")
    Optional<Voucher> findValidVoucher(@Param("code") String code, @Param("now") LocalDateTime now);

    // Lấy tất cả voucher hoạt động
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND (v.startDate IS NULL OR v.startDate <= :now) " +
           "AND (v.endDate IS NULL OR v.endDate >= :now)")
    java.util.List<Voucher> findAllActiveVouchers(@Param("now") LocalDateTime now);
}
