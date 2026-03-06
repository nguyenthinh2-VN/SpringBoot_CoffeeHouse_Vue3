package SpringBoot.demo.Service.Admin;

import SpringBoot.demo.DTO.*;
import SpringBoot.demo.Enum.DeliveryMethod;
import SpringBoot.demo.Enum.OrderStatus;
import SpringBoot.demo.Enum.PaymentStatus;
import SpringBoot.demo.Enum.Role;
import SpringBoot.demo.Model.Order;
import SpringBoot.demo.Model.OrderStatusLog;
import SpringBoot.demo.Model.User;
import SpringBoot.demo.Repository.OrderRepository;
import SpringBoot.demo.Repository.OrderStatusLogRepository;
import SpringBoot.demo.Service.Order.OrderItemService;
import SpringBoot.demo.Service.Order.Pricing.StaffPricingStrategy;
import SpringBoot.demo.Service.Order.Validation.StaffOrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service quản lý đơn hàng staff/admin
 * SRP: Chỉ xử lý staff order operations
 * 
 * Trách nhiệm:
 * - Tạo đơn tại quán
 * - Xem danh sách đơn
 * - Xem chi tiết đơn
 * - Xác nhận, hoàn thành, giao hàng, từ chối đơn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {
    
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final OrderItemService orderItemService;
    private final StaffPricingStrategy staffPricingStrategy;
    private final StaffOrderValidator staffOrderValidator;
    private final ModelMapper modelMapper;
    
    /**
     * Tạo đơn hàng tại quán (staff/admin)
     */
    @Transactional
    public ApiResponse<OrderResponse> createStaffOrder(CreateStaffOrderRequest request) {
        try {
            // 0. Lấy staffId từ JWT token (authenticated user)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                log.warn("User not authenticated");
                return ApiResponse.error("Vui lòng đăng nhập");
            }
            
            User staff = (User) auth.getPrincipal();
            Long staffId = staff.getId();
            
            log.info("Creating staff order by staff: {}", staffId);
            
            // 1. Kiểm tra staff có quyền không (role = STAFF hoặc ADMIN)
            if (staff.getRole() != Role.STAFF && staff.getRole() != Role.ADMIN) {
                log.warn("User is not staff/admin: id={}, role={}", staffId, staff.getRole());
                return ApiResponse.error("Người dùng không có quyền tạo đơn hàng");
            }
            log.info("Staff role verified: id={}, role={}", staff.getId(), staff.getRole());
            
            // 1. Validate request
            staffOrderValidator.validate(request);
            
            // 2. Tính subtotal từ items
            BigDecimal subtotal = BigDecimal.ZERO;
            for (OrderItemRequest itemRequest : request.getItems()) {
                BigDecimal itemPrice = orderItemService.calculateItemPrice(itemRequest);
                subtotal = subtotal.add(itemPrice);
                log.info("Item price calculated: {}", itemPrice);
            }
            log.info("Subtotal calculated: {}", subtotal);
            
            // 3. Tính tax (staff: 10%)
            BigDecimal tax = staffPricingStrategy.calculateTax(subtotal);
            
            // 4. Tính shipping fee (staff: 0)
            BigDecimal shippingFee = staffPricingStrategy.calculateShippingFee(null, null);
            
            // 5. Tính discount (staff: 0)
            BigDecimal discount = staffPricingStrategy.calculateDiscount(null, subtotal);
            
            // 6. Tính total
            BigDecimal totalAmount = staffPricingStrategy.calculateTotal(subtotal, tax, shippingFee, discount);
            
            log.info("=== STAFF ORDER CALCULATION ===");
            log.info("Subtotal: {}", subtotal);
            log.info("Tax (10%): {}", tax);
            log.info("ShippingFee: {}", shippingFee);
            log.info("Discount: {}", discount);
            log.info("Total: {}", totalAmount);
            log.info("=== END CALCULATION ===");
            
            // 7. Tạo Order entity
            Order order = new Order();
            order.setUserId(staffId); // Staff order: userId = staffId (người tạo đơn)
            order.setOrderCode(generateOrderCode());
            order.setTotalAmount(totalAmount);
            order.setTax(tax);
            order.setShippingFee(shippingFee);
            order.setDiscount(discount);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentMethod(request.getPaymentMethod());
            order.setDeliveryMethod(DeliveryMethod.PICKUP); // Staff order mặc định PICKUP
            order.setCreatedByStaffId(staffId); // Audit: staff tạo đơn (từ JWT token)
            order.setNotes(request.getNotes());
            order.setCreatedAt(LocalDateTime.now());
            
            // 8. Lưu Order
            Order savedOrder = orderRepository.save(order);
            log.info("Staff order saved: id={}, code={}", savedOrder.getId(), savedOrder.getOrderCode());
            
            // 9. Tạo status log
            OrderStatusLog statusLog = new OrderStatusLog(
                savedOrder, 
                OrderStatus.PENDING, 
                "Đơn hàng tại quán được tạo bởi staff ID: " + staffId
            );
            orderStatusLogRepository.save(statusLog);
            
            log.info("Staff order created successfully: {}", savedOrder.getOrderCode());
            
            OrderResponse response = modelMapper.map(savedOrder, OrderResponse.class);
            return ApiResponse.success("Tạo đơn hàng thành công", response);
            
        } catch (Exception e) {
            log.error("Error creating staff order: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi tạo đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Lấy danh sách tất cả đơn hàng
     */
    public ApiResponse<Page<OrderResponse>> getAllOrders(Pageable pageable) {
        try {
            log.info("Getting all orders");
            Page<Order> orders = orderRepository.findAll(pageable);
            Page<OrderResponse> responses = orders.map(order -> modelMapper.map(order, OrderResponse.class));
            return ApiResponse.success("Lấy danh sách đơn hàng thành công", responses);
        } catch (Exception e) {
            log.error("Error getting orders: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Lấy chi tiết đơn hàng
     */
    public ApiResponse<OrderDetailResponse> getOrderDetail(Long orderId) {
        try {
            log.info("Getting order detail: {}", orderId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                log.warn("Order not found: {}", orderId);
                return ApiResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            OrderDetailResponse response = modelMapper.map(order, OrderDetailResponse.class);
            
            return ApiResponse.success("Lấy chi tiết đơn hàng thành công", response);
        } catch (Exception e) {
            log.error("Error getting order detail: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi lấy chi tiết đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Xác nhận đơn hàng (PENDING → CONFIRMED)
     */
    @Transactional
    public ApiResponse<OrderResponse> confirmOrder(Long orderId, String reason) {
        try {
            log.info("Confirming order: {}", orderId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                return ApiResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            if (order.getStatus() != OrderStatus.PENDING) {
                log.warn("Order status is not PENDING: {}", order.getStatus());
                return ApiResponse.error("Chỉ có thể xác nhận đơn ở trạng thái chờ duyệt");
            }
            
            // Cập nhật status
            order.setStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);
            
            // Tạo status log
            OrderStatusLog statusLog = new OrderStatusLog(
                updatedOrder, 
                OrderStatus.CONFIRMED, 
                reason != null ? reason : "Đơn hàng được xác nhận"
            );
            orderStatusLogRepository.save(statusLog);
            
            log.info("Order confirmed: {}", orderId);
            OrderResponse response = modelMapper.map(updatedOrder, OrderResponse.class);
            return ApiResponse.success("Xác nhận đơn hàng thành công", response);
            
        } catch (Exception e) {
            log.error("Error confirming order: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi xác nhận đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Hoàn thành đơn hàng (CONFIRMED → COMPLETED) - Dành cho PICKUP
     */
    @Transactional
    public ApiResponse<OrderResponse> completeOrder(Long orderId, String reason) {
        try {
            log.info("Completing order: {}", orderId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                return ApiResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            if (order.getStatus() != OrderStatus.CONFIRMED) {
                log.warn("Order status is not CONFIRMED: {}", order.getStatus());
                return ApiResponse.error("Chỉ có thể hoàn thành đơn ở trạng thái đã xác nhận");
            }
            
            if (order.getDeliveryMethod() != DeliveryMethod.PICKUP) {
                log.warn("Order is not PICKUP: {}", order.getDeliveryMethod());
                return ApiResponse.error("Chỉ có thể hoàn thành đơn PICKUP");
            }
            
            // Cập nhật status
            order.setStatus(OrderStatus.COMPLETED);
            order.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);
            
            // Tạo status log
            OrderStatusLog statusLog = new OrderStatusLog(
                updatedOrder, 
                OrderStatus.COMPLETED, 
                reason != null ? reason : "Khách đã nhận hàng"
            );
            orderStatusLogRepository.save(statusLog);
            
            log.info("Order completed: {}", orderId);
            OrderResponse response = modelMapper.map(updatedOrder, OrderResponse.class);
            return ApiResponse.success("Hoàn thành đơn hàng thành công", response);
            
        } catch (Exception e) {
            log.error("Error completing order: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi hoàn thành đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Bắt đầu giao hàng (CONFIRMED → SHIPPING) - Dành cho DELIVERY
     */
    @Transactional
    public ApiResponse<OrderResponse> shipOrder(Long orderId, String reason) {
        try {
            log.info("Shipping order: {}", orderId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                return ApiResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            if (order.getStatus() != OrderStatus.CONFIRMED) {
                log.warn("Order status is not CONFIRMED: {}", order.getStatus());
                return ApiResponse.error("Chỉ có thể giao đơn ở trạng thái đã xác nhận");
            }
            
            // Cập nhật status
            order.setStatus(OrderStatus.SHIPPING);
            order.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);
            
            // Tạo status log
            OrderStatusLog statusLog = new OrderStatusLog(
                updatedOrder, 
                OrderStatus.SHIPPING, 
                reason != null ? reason : "Đơn hàng đang được giao"
            );
            orderStatusLogRepository.save(statusLog);
            
            log.info("Order shipped: {}", orderId);
            OrderResponse response = modelMapper.map(updatedOrder, OrderResponse.class);
            return ApiResponse.success("Bắt đầu giao hàng thành công", response);
            
        } catch (Exception e) {
            log.error("Error shipping order: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi bắt đầu giao hàng: " + e.getMessage());
        }
    }
    
    /**
     * Giao hàng thành công (SHIPPING → DELIVERED)
     */
    @Transactional
    public ApiResponse<OrderResponse> deliverOrder(Long orderId, String reason) {
        try {
            log.info("Delivering order: {}", orderId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                return ApiResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            if (order.getStatus() != OrderStatus.SHIPPING) {
                log.warn("Order status is not SHIPPING: {}", order.getStatus());
                return ApiResponse.error("Chỉ có thể hoàn thành giao đơn ở trạng thái đang giao");
            }
            
            // Cập nhật status
            order.setStatus(OrderStatus.DELIVERED);
            order.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);
            
            // Tạo status log
            OrderStatusLog statusLog = new OrderStatusLog(
                updatedOrder, 
                OrderStatus.DELIVERED, 
                reason != null ? reason : "Đơn hàng đã giao thành công"
            );
            orderStatusLogRepository.save(statusLog);
            
            log.info("Order delivered: {}", orderId);
            OrderResponse response = modelMapper.map(updatedOrder, OrderResponse.class);
            return ApiResponse.success("Giao hàng thành công", response);
            
        } catch (Exception e) {
            log.error("Error delivering order: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi giao hàng: " + e.getMessage());
        }
    }
    
    /**
     * Từ chối đơn hàng (PENDING → CANCELLED)
     */
    @Transactional
    public ApiResponse<OrderResponse> rejectOrder(Long orderId, String reason) {
        try {
            log.info("Rejecting order: {}", orderId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                return ApiResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            if (order.getStatus() != OrderStatus.PENDING) {
                log.warn("Order status is not PENDING: {}", order.getStatus());
                return ApiResponse.error("Chỉ có thể từ chối đơn ở trạng thái chờ duyệt");
            }
            
            // Cập nhật status
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);
            
            // Tạo status log
            OrderStatusLog statusLog = new OrderStatusLog(
                updatedOrder, 
                OrderStatus.CANCELLED, 
                reason != null ? reason : "Đơn hàng bị từ chối"
            );
            orderStatusLogRepository.save(statusLog);
            
            log.info("Order rejected: {}", orderId);
            OrderResponse response = modelMapper.map(updatedOrder, OrderResponse.class);
            return ApiResponse.success("Từ chối đơn hàng thành công", response);
            
        } catch (Exception e) {
            log.error("Error rejecting order: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi từ chối đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Xác nhận thanh toán COD
     */
    @Transactional
    public ApiResponse<OrderResponse> confirmPaymentCOD(Long orderId) {
        try {
            log.info("Confirming COD payment for order: {}", orderId);
            
            // 1. Lấy order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
            
            // 2. Kiểm tra trạng thái thanh toán
            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                log.warn("Order already paid: {}", orderId);
                return ApiResponse.error("Đơn hàng đã được thanh toán");
            }
            
            // 3. Update payment status
            order.setPaymentStatus(PaymentStatus.PAID);
            log.info("Payment status updated to PAID");
            
            // 4. Update order status: PENDING → CONFIRMED
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("Order status updated to CONFIRMED");
            
            // 5. Lưu order
            Order updatedOrder = orderRepository.save(order);
            
            // 6. Tạo status log
            OrderStatusLog statusLog = new OrderStatusLog(
                updatedOrder,
                OrderStatus.CONFIRMED,
                "Thanh toán COD xác nhận - Đơn hàng được xác nhận"
            );
            orderStatusLogRepository.save(statusLog);
            
            log.info("COD payment confirmed successfully: {}", orderId);
            OrderResponse response = modelMapper.map(updatedOrder, OrderResponse.class);
            return ApiResponse.success("Xác nhận thanh toán thành công", response);
            
        } catch (Exception e) {
            log.error("Error confirming COD payment: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi xác nhận thanh toán: " + e.getMessage());
        }
    }
    
    /**
     * Helper: Tạo mã đơn hàng
     */
    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }
}
