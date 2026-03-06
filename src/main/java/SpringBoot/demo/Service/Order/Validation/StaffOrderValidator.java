package SpringBoot.demo.Service.Order.Validation;

import SpringBoot.demo.DTO.CreateStaffOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator cho đơn hàng staff (tại quán)
 * SRP: Chỉ validate staff order
 * 
 * Kiểm tra:
 * - Items không rỗng
 * 
 * Note: Staff ID được lấy từ JWT token, không cần validate từ request
 */
@Slf4j
@Component
public class StaffOrderValidator implements OrderValidator {
    
    @Override
    public void validate(Object request) {
        CreateStaffOrderRequest req = (CreateStaffOrderRequest) request;
        
        // Validate items
        if (req.getItems() == null || req.getItems().isEmpty()) {
            log.warn("Staff order validation failed: Items list is empty");
            throw new RuntimeException("Danh sách sản phẩm không được để trống");
        }
        
        log.info("Staff order validation passed");
    }
}
