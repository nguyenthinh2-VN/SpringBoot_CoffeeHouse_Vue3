package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.*;
import SpringBoot.demo.Enum.PaymentMethod;
import SpringBoot.demo.Model.Order;
import SpringBoot.demo.Model.User;
import SpringBoot.demo.Repository.OrderRepository;
import SpringBoot.demo.Repository.User.UserRepository;
import SpringBoot.demo.Service.Order.OrderService;
import SpringBoot.demo.Service.Payment.PaymentResult;
import SpringBoot.demo.Service.Payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "APIs quản lý đơn hàng")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentService paymentService;

    /**
     * Tạo đơn hàng mới
     */
    @Operation(summary = "Tạo đơn hàng mới", description = "Khách hàng tạo đơn hàng sau khi chọn sản phẩm")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo đơn hàng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            // Lấy userId từ JWT token
            Long userId = extractUserIdFromToken();
            if (userId == null) {
                ApiResponse<OrderResponse> errorResponse = ApiResponse.error("Bạn phải đăng nhập để đặt hàng");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            log.info("Creating order for user: {}", userId);
            ApiResponse<OrderResponse> response = orderService.createOrder(request, userId);

            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            ApiResponse<OrderResponse> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * VNPAY callback handler - Must be before /{orderId} to avoid path conflict
     * Frontend handles redirect, Backend only confirms payment
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<ApiResponse<String>> vnpayCallback(@RequestParam Map<String, String> params) {
        try {
            log.info("Received VNPAY callback with params: {}", params);

            var callbackResult = paymentService.handleCallback(PaymentMethod.VNPAY, params);

            if (callbackResult.isSuccess()) {
                String orderId = params.get("vnp_OrderInfo");
                log.info("Payment successful for order: {}", orderId);
                return ResponseEntity.ok(ApiResponse.success("Thanh toán thành công", "OK"));
            } else {
                log.warn("Payment failed: {}", callbackResult.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(callbackResult.getMessage()));
            }

        } catch (Exception e) {
            log.error("Error handling VNPAY callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi xử lý callback: " + e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    @Operation(summary = "Lấy chi tiết đơn hàng", description = "Lấy thông tin chi tiết một đơn hàng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable Long orderId) {
        try {
            Long userId = extractUserIdFromToken();
            if (userId == null) {
                ApiResponse<OrderDetailResponse> errorResponse = ApiResponse.error("Bạn phải đăng nhập");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            log.info("Getting order detail: {} for user: {}", orderId, userId);
            ApiResponse<OrderDetailResponse> response = orderService.getOrderDetail(orderId, userId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("Error getting order detail: {}", e.getMessage(), e);
            ApiResponse<OrderDetailResponse> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lấy danh sách đơn hàng của user
     */
    @Operation(summary = "Lấy danh sách đơn hàng", description = "Lấy tất cả đơn hàng của user hiện tại")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders() {
        try {
            Long userId = extractUserIdFromToken();
            if (userId == null) {
                ApiResponse<List<OrderResponse>> errorResponse = ApiResponse.error("Bạn phải đăng nhập");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            log.info("Getting orders for user: {}", userId);
            ApiResponse<List<OrderResponse>> response = orderService.getOrdersByUser(userId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("Error getting user orders: {}", e.getMessage(), e);
            ApiResponse<List<OrderResponse>> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Hủy đơn hàng
     */
    @Operation(summary = "Hủy đơn hàng", description = "Khách hàng hủy đơn hàng (chỉ hủy được đơn PENDING hoặc CONFIRMED)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hủy thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể hủy"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại")
    })
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(@PathVariable Long orderId) {
        try {
            Long userId = extractUserIdFromToken();
            if (userId == null) {
                ApiResponse<String> errorResponse = ApiResponse.error("Bạn phải đăng nhập");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            log.info("Cancelling order: {} for user: {}", orderId, userId);
            ApiResponse<String> response = orderService.cancelOrder(orderId, userId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage(), e);
            ApiResponse<String> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lấy lịch sử thay đổi trạng thái
     */
    @Operation(summary = "Lấy lịch sử trạng thái", description = "Lấy lịch sử thay đổi trạng thái của đơn hàng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại")
    })
    @GetMapping("/{orderId}/status-log")
    public ResponseEntity<ApiResponse<List<OrderStatusLogResponse>>> getOrderStatusLog(@PathVariable Long orderId) {
        try {
            Long userId = extractUserIdFromToken();
            if (userId == null) {
                ApiResponse<List<OrderStatusLogResponse>> errorResponse = ApiResponse.error("Bạn phải đăng nhập");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            log.info("Getting status log for order: {} for user: {}", orderId, userId);
            ApiResponse<List<OrderStatusLogResponse>> response = orderService.getOrderStatusLog(orderId, userId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("Error getting order status log: {}", e.getMessage(), e);
            ApiResponse<List<OrderStatusLogResponse>> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create VNPAY payment link
     */
    @Operation(summary = "Tạo link thanh toán VNPAY", description = "Tạo link thanh toán VNPAY cho đơn hàng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo link thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Đơn hàng không tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping("/{orderId}/vnpay-payment")
    public ResponseEntity<ApiResponse<PaymentResult>> createVNPayPaymentLink(@PathVariable Long orderId) {
        try {
            Long userId = extractUserIdFromToken();
            if (userId == null) {
                ApiResponse<PaymentResult> errorResponse = ApiResponse.error("Bạn phải đăng nhập");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            log.info("Creating VNPAY payment link for order: {} user: {}", orderId, userId);

            // Get order
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                ApiResponse<PaymentResult> errorResponse = ApiResponse.error("Đơn hàng không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Check order belongs to user
            if (!order.getUserId().equals(userId)) {
                ApiResponse<PaymentResult> errorResponse = ApiResponse.error("Không có quyền truy cập đơn hàng này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Process payment
            PaymentResult result = paymentService.processPayment(order);

            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("Tạo link thanh toán thành công", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(result.getMessage()));
            }

        } catch (Exception e) {
            log.error("Error creating VNPAY payment link: {}", e.getMessage(), e);
            ApiResponse<PaymentResult> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /**
     * Lấy userId từ JWT token
     */
    private Long extractUserIdFromToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsernameOrEmail(username, username);
                if (userOpt.isPresent()) {
                    return userOpt.get().getId();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            return null;
        }
    }
}
