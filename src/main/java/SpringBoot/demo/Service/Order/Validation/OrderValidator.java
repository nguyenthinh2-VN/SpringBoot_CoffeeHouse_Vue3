package SpringBoot.demo.Service.Order.Validation;

/**
 * Validator interface cho đơn hàng
 * SRP: Định nghĩa contract cho các validator khác nhau
 * 
 * Dễ mở rộng: Thêm loại order mới chỉ cần implement interface này
 */
public interface OrderValidator {
    
    /**
     * Validate request
     * @param request Request object
     * @throws RuntimeException nếu validation fail
     */
    void validate(Object request);
}
