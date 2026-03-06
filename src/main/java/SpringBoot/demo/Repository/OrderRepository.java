package SpringBoot.demo.Repository;

import SpringBoot.demo.Enum.OrderStatus;
import SpringBoot.demo.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy đơn hàng theo mã đơn
    Optional<Order> findByOrderCode(String orderCode);

    // Lấy tất cả đơn hàng của user
    List<Order> findByUserId(Long userId);

    // Lấy đơn hàng của user theo trạng thái
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    // Lấy đơn hàng theo trạng thái
    List<Order> findByStatus(OrderStatus status);

    // Lấy đơn hàng của user sắp xếp theo ngày tạo (mới nhất trước)
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findUserOrdersLatest(@Param("userId") Long userId);

    // Lấy đơn hàng trong khoảng thời gian
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Kiểm tra mã đơn hàng đã tồn tại chưa
    boolean existsByOrderCode(String orderCode);

    // Đếm số đơn hàng của user
    long countByUserId(Long userId);

    // Đếm số đơn hàng theo trạng thái
    long countByStatus(OrderStatus status);
}
