package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.DTO.OrderPreviewRequest;
import SpringBoot.demo.DTO.OrderPreviewResponse;
import SpringBoot.demo.Service.Order.OrderPreviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller preview giá đơn hàng
 * SRP: Chỉ handle preview API
 * 
 * Endpoint: POST /api/orders/preview
 * - User xem giá trước khi đặt hàng
 * - Không tạo đơn, chỉ tính giá
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderPreviewController {
    
    private final OrderPreviewService orderPreviewService;
    
    /**
     * Preview giá đơn hàng
     * 
     * @param request OrderPreviewRequest (items, addressId, voucherCode, deliveryMethod)
     * @return OrderPreviewResponse (subtotal, tax, shippingFee, discount, total)
     */
    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> previewOrder(
            @RequestBody OrderPreviewRequest request) {
        
        try {
            log.info("Preview order request received");
            
            // Validate request
            if (request.getItems() == null || request.getItems().isEmpty()) {
                log.warn("Items list is empty");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Danh sách sản phẩm không được để trống"));
            }
            
            // Preview order
            OrderPreviewResponse response = orderPreviewService.previewOrder(request);
            
            log.info("Order preview completed: total={}", response.getTotalAmount());
            return ResponseEntity.ok(ApiResponse.success("Preview giá thành công", response));
            
        } catch (Exception e) {
            log.error("Error previewing order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Lỗi preview giá: " + e.getMessage()));
        }
    }
}
