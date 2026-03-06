package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.AddressRequest;
import SpringBoot.demo.DTO.AddressResponse;
import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.Repository.User.UserRepository;
import SpringBoot.demo.Service.Address.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Tạo địa chỉ mới
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @RequestBody AddressRequest request,
            Authentication authentication) {
        try {
            log.info("Creating address for user: {}", authentication.getName());

            Long userId = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            ApiResponse<AddressResponse> response = addressService.createAddress(request, userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi tạo địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách địa chỉ của user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            Authentication authentication) {
        try {
            log.info("Getting addresses for user: {}", authentication.getName());

            Long userId = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            ApiResponse<List<AddressResponse>> response = addressService.getAddressesByUser(userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting addresses: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi lấy danh sách địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết địa chỉ
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressDetail(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            log.info("Getting address detail: id={}", id);

            Long userId = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            ApiResponse<AddressResponse> response = addressService.getAddressDetail(id, userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting address detail: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi lấy chi tiết địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật địa chỉ
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Integer id,
            @RequestBody AddressRequest request,
            Authentication authentication) {
        try {
            log.info("Updating address: id={}", id);

            Long userId = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            ApiResponse<AddressResponse> response = addressService.updateAddress(id, request, userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi cập nhật địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Xóa địa chỉ
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            log.info("Deleting address: id={}", id);

            Long userId = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            ApiResponse<String> response = addressService.deleteAddress(id, userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi xóa địa chỉ: " + e.getMessage()));
        }
    }

    /**
     * Lấy địa chỉ mặc định
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress(
            Authentication authentication) {
        try {
            log.info("Getting default address for user: {}", authentication.getName());

            Long userId = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            ApiResponse<AddressResponse> response = addressService.getDefaultAddress(userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting default address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi lấy địa chỉ mặc định: " + e.getMessage()));
        }
    }
}
