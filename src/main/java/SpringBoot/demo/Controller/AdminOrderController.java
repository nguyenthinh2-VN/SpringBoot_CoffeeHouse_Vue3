package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.DTO.CreateStaffOrderRequest;
import SpringBoot.demo.DTO.OrderDetailResponse;
import SpringBoot.demo.DTO.OrderResponse;
import SpringBoot.demo.Service.Admin.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý đơn hàng staff/admin
 * SRP: Chỉ handle admin order APIs
 * 
 * Endpoints:
 * - POST /api/admin/orders - Tạo đơn tại quán
 * - GET /api/admin/orders - Danh sách đơn
 * - GET /api/admin/orders/{id} - Chi tiết đơn
 * - PUT /api/admin/orders/{id}/confirm - Xác nhận
 * - PUT /api/admin/orders/{id}/complete - Hoàn thành (PICKUP)
 * - PUT /api/admin/orders/{id}/ship - Giao hàng (DELIVERY)
 * - PUT /api/admin/orders/{id}/deliver - Giao thành công
 * - PUT /api/admin/orders/{id}/reject - Từ chối
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class AdminOrderController {
    
    private final AdminOrderService adminOrderService;
    
    /**
     * Tạo đơn hàng tại quán
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createStaffOrder(
            @RequestBody CreateStaffOrderRequest request) {
        
        try {
            log.info("Creating staff order");
            ApiResponse<OrderResponse> response = adminOrderService.createStaffOrder(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error creating staff order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi tạo đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách tất cả đơn hàng
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(Pageable pageable) {
        try {
            log.info("Getting all orders");
            ApiResponse<Page<OrderResponse>> response = adminOrderService.getAllOrders(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting orders: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi lấy danh sách đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable Long id) {
        try {
            log.info("Getting order detail: {}", id);
            ApiResponse<OrderDetailResponse> response = adminOrderService.getOrderDetail(id);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error getting order detail: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi lấy chi tiết đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Xác nhận đơn hàng (PENDING → CONFIRMED)
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        try {
            log.info("Confirming order: {}", id);
            ApiResponse<OrderResponse> response = adminOrderService.confirmOrder(id, reason);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error confirming order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi xác nhận đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Hoàn thành đơn hàng (CONFIRMED → COMPLETED) - Dành cho PICKUP (Nhận nước tại quán thành công)
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        try {
            log.info("Completing order: {}", id);
            ApiResponse<OrderResponse> response = adminOrderService.completeOrder(id, reason);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error completing order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi hoàn thành đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Bắt đầu giao hàng (CONFIRMED → SHIPPING) - Dành cho DELIVERY (Chờ Shipper nhận đơn đi giao)
     */
    @PutMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        try {
            log.info("Shipping order: {}", id);
            ApiResponse<OrderResponse> response = adminOrderService.shipOrder(id, reason);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error shipping order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi bắt đầu giao hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Giao hàng thành công (SHIPPING → DELIVERED)  (Shipper giao hàng thành công đến khách)
     */
    @PutMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        try {
            log.info("Delivering order: {}", id);
            ApiResponse<OrderResponse> response = adminOrderService.deliverOrder(id, reason);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error delivering order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi giao hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Từ chối đơn hàng (PENDING → CANCELLED)
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<OrderResponse>> rejectOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        try {
            log.info("Rejecting order: {}", id);
            ApiResponse<OrderResponse> response = adminOrderService.rejectOrder(id, reason);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error rejecting order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi từ chối đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Xác nhận thanh toán COD (PENDING → CONFIRMED + PAID)
     */
    @PutMapping("/{id}/confirm-payment-cod")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPaymentCOD(@PathVariable Long id) {
        try {
            log.info("Confirming COD payment for order: {}", id);
            ApiResponse<OrderResponse> response = adminOrderService.confirmPaymentCOD(id);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error confirming COD payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi xác nhận thanh toán: " + e.getMessage()));
        }
    }
}
