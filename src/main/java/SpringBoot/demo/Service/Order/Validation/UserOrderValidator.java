package SpringBoot.demo.Service.Order.Validation;

import SpringBoot.demo.DTO.CreateUserOrderRequest;
import SpringBoot.demo.Service.Address.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator cho đơn hàng user (online)
 * SRP: Chỉ validate user order
 * 
 * Kiểm tra:
 * - Items không rỗng
 * - Address ID bắt buộc
 * - Address tồn tại và thuộc user
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserOrderValidator implements OrderValidator {
    
    private final AddressService addressService;
    
    @Override
    public void validate(Object request) {
        CreateUserOrderRequest req = (CreateUserOrderRequest) request;
        
        // Validate items
        if (req.getItems() == null || req.getItems().isEmpty()) {
            log.warn("User order validation failed: Items list is empty");
            throw new RuntimeException("Danh sách sản phẩm không được để trống");
        }
        
        // Validate address ID
        if (req.getAddressId() == null) {
            log.warn("User order validation failed: Address ID is null");
            throw new RuntimeException("Địa chỉ giao hàng không được để trống");
        }
        
        log.info("User order validation passed");
    }
    
    /**
     * Validate address belongs to user
     */
    public void validateAddressOwnership(Integer addressId, Long userId) {
        if (!addressService.isAddressOwnedByUser(addressId, userId)) {
            log.warn("Address not owned by user: addressId={}, userId={}", addressId, userId);
            throw new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc về bạn");
        }
    }
}
