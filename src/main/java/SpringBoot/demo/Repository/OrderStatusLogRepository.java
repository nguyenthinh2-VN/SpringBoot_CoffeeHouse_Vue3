package SpringBoot.demo.Repository;

import SpringBoot.demo.Enum.OrderStatus;
import SpringBoot.demo.Model.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long> {

    // Lấy lịch sử thay đổi trạng thái của một đơn hàng
    @Query("SELECT osl FROM OrderStatusLog osl WHERE osl.order.id = :orderId ORDER BY osl.createdAt DESC")
    List<OrderStatusLog> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);

    // Lấy log theo trạng thái
    List<OrderStatusLog> findByStatus(OrderStatus status);

    // Đếm số lần thay đổi trạng thái của một đơn hàng
    long countByOrderId(Long orderId);
}
