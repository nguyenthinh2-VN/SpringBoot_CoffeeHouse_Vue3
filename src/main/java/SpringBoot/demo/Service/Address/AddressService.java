package SpringBoot.demo.Service.Address;

import SpringBoot.demo.DTO.AddressRequest;
import SpringBoot.demo.DTO.AddressResponse;
import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.Model.Address;
import SpringBoot.demo.Repository.AddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Tạo địa chỉ mới
     */
    @Transactional
    public ApiResponse<AddressResponse> createAddress(AddressRequest request, Long userId) {
        try {
            log.info("Creating address for user: {}", userId);

            // Nếu isDefault = true, bỏ default của địa chỉ cũ
            if (request.getIsDefault()) {
                addressRepository.findDefaultByUserId(userId).ifPresent(addr -> {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                    log.info("Removed default flag from old address");
                });
            }

            // Tạo địa chỉ mới
            Address address = new Address();
            address.setUserId(userId);
            address.setFullName(request.getFullName());
            address.setPhone(request.getPhone());
            address.setDistrict(request.getDistrict());
            address.setCity(request.getCity());
            address.setFullAddress(request.getFullAddress());
            address.setIsDefault(request.getIsDefault());
            address.setCreatedAt(LocalDateTime.now());

            Address savedAddress = addressRepository.save(address);
            log.info("Address created successfully: id={}", savedAddress.getId());

            AddressResponse response = modelMapper.map(savedAddress, AddressResponse.class);
            return ApiResponse.success("Tạo địa chỉ thành công", response);

        } catch (Exception e) {
            log.error("Error creating address: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi tạo địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách địa chỉ của user
     */
    public ApiResponse<List<AddressResponse>> getAddressesByUser(Long userId) {
        try {
            log.info("Getting addresses for user: {}", userId);

            List<Address> addresses = addressRepository.findByUserId(userId);
            List<AddressResponse> responses = addresses.stream()
                    .map(addr -> modelMapper.map(addr, AddressResponse.class))
                    .collect(Collectors.toList());

            log.info("Found {} addresses for user: {}", responses.size(), userId);
            return ApiResponse.success("Lấy danh sách địa chỉ thành công", responses);

        } catch (Exception e) {
            log.error("Error getting addresses: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi lấy danh sách địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết địa chỉ
     */
    public ApiResponse<AddressResponse> getAddressDetail(Integer addressId, Long userId) {
        try {
            log.info("Getting address detail: id={}, userId={}", addressId, userId);

            Optional<Address> addressOpt = addressRepository.findByIdAndUserId(addressId, userId);
            if (addressOpt.isEmpty()) {
                log.warn("Address not found or not owned by user: id={}, userId={}", addressId, userId);
                return ApiResponse.error("Địa chỉ không tồn tại");
            }

            AddressResponse response = modelMapper.map(addressOpt.get(), AddressResponse.class);
            return ApiResponse.success("Lấy chi tiết địa chỉ thành công", response);

        } catch (Exception e) {
            log.error("Error getting address detail: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi lấy chi tiết địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Cập nhật địa chỉ
     */
    @Transactional
    public ApiResponse<AddressResponse> updateAddress(Integer addressId, AddressRequest request, Long userId) {
        try {
            log.info("Updating address: id={}, userId={}", addressId, userId);

            Optional<Address> addressOpt = addressRepository.findByIdAndUserId(addressId, userId);
            if (addressOpt.isEmpty()) {
                log.warn("Address not found or not owned by user: id={}, userId={}", addressId, userId);
                return ApiResponse.error("Địa chỉ không tồn tại");
            }

            Address address = addressOpt.get();

            // Nếu isDefault = true, bỏ default của địa chỉ cũ
            if (request.getIsDefault() && !address.getIsDefault()) {
                addressRepository.findDefaultByUserId(userId).ifPresent(addr -> {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                    log.info("Removed default flag from old address");
                });
            }

            // Cập nhật
            address.setFullName(request.getFullName());
            address.setPhone(request.getPhone());
            address.setDistrict(request.getDistrict());
            address.setCity(request.getCity());
            address.setFullAddress(request.getFullAddress());
            address.setIsDefault(request.getIsDefault());

            Address updatedAddress = addressRepository.save(address);
            log.info("Address updated successfully: id={}", updatedAddress.getId());

            AddressResponse response = modelMapper.map(updatedAddress, AddressResponse.class);
            return ApiResponse.success("Cập nhật địa chỉ thành công", response);

        } catch (Exception e) {
            log.error("Error updating address: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi cập nhật địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Xóa địa chỉ
     */
    @Transactional
    public ApiResponse<String> deleteAddress(Integer addressId, Long userId) {
        try {
            log.info("Deleting address: id={}, userId={}", addressId, userId);

            Optional<Address> addressOpt = addressRepository.findByIdAndUserId(addressId, userId);
            if (addressOpt.isEmpty()) {
                log.warn("Address not found or not owned by user: id={}, userId={}", addressId, userId);
                return ApiResponse.error("Địa chỉ không tồn tại");
            }

            addressRepository.deleteById(addressId);
            log.info("Address deleted successfully: id={}", addressId);

            return ApiResponse.success("Xóa địa chỉ thành công");

        } catch (Exception e) {
            log.error("Error deleting address: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi xóa địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Lấy địa chỉ mặc định của user
     */
    public ApiResponse<AddressResponse> getDefaultAddress(Long userId) {
        try {
            log.info("Getting default address for user: {}", userId);

            Optional<Address> addressOpt = addressRepository.findDefaultByUserId(userId);
            if (addressOpt.isEmpty()) {
                log.warn("No default address found for user: {}", userId);
                return ApiResponse.error("Chưa có địa chỉ mặc định");
            }

            AddressResponse response = modelMapper.map(addressOpt.get(), AddressResponse.class);
            return ApiResponse.success("Lấy địa chỉ mặc định thành công", response);

        } catch (Exception e) {
            log.error("Error getting default address: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi lấy địa chỉ mặc định: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra địa chỉ có thuộc user không
     */
    public boolean isAddressOwnedByUser(Integer addressId, Long userId) {
        return addressRepository.existsByIdAndUserId(addressId, userId);
    }

    /**
     * Lấy Address entity (dùng trong OrderService)
     */
    public Optional<Address> getAddressById(Integer addressId) {
        return addressRepository.findById(addressId);
    }
}
