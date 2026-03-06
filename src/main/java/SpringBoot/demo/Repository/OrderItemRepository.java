package SpringBoot.demo.Repository;

import SpringBoot.demo.Model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Lấy tất cả item của một đơn hàng
    List<OrderItem> findByOrderId(Long orderId);

    // Lấy item theo order và product
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.productId = :productId")
    List<OrderItem> findByOrderAndProduct(@Param("orderId") Long orderId, @Param("productId") Integer productId);

    // Đếm số item trong đơn hàng
    long countByOrderId(Long orderId);
}
