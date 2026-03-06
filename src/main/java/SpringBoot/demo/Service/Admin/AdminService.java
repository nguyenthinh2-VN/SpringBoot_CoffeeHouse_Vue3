package SpringBoot.demo.Service.Admin;

import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.DTO.ProductRequest;
import SpringBoot.demo.DTO.ProductResponse;
import SpringBoot.demo.Model.Product;
import SpringBoot.demo.Repository.Product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private ProductRepository productRepository;

    // Thêm sản phẩm mới (chỉ ADMIN)
    public ApiResponse<ProductResponse> addProduct(ProductRequest request) {
        try {
            // Validate input
            if (request.getTensp() == null || request.getTensp().trim().isEmpty()) {
                return ApiResponse.error("Tên sản phẩm không được để trống");
            }

            if (request.getGia() == null || request.getGia() <= 0) {
                return ApiResponse.error("Giá sản phẩm phải lớn hơn 0");
            }

            if (request.getCategoryId() == null) {
                return ApiResponse.error("Category ID không được để trống");
            }

            // Tạo Product entity từ request
            Product product = new Product();
            product.setTensp(request.getTensp().trim());
            product.setGia(request.getGia());
            product.setCategory_id(request.getCategoryId());
            product.setHinh(request.getHinh() != null ? request.getHinh().trim() : "");

            // Lưu vào database
            Product savedProduct = productRepository.save(product);

            // Convert thành response DTO
            ProductResponse response = ProductResponse.fromProduct(savedProduct);

            return ApiResponse.success("Thêm sản phẩm thành công", response);

        } catch (Exception e) {
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Cập nhật sản phẩm (chỉ ADMIN)
    public ApiResponse<ProductResponse> updateProduct(Integer productId, ProductRequest request) {
        try {
            // Kiểm tra sản phẩm có tồn tại không
            if (!productRepository.existsById(productId)) {
                return ApiResponse.error("Sản phẩm không tồn tại với ID: " + productId);
            }

            // Validate input
            if (request.getTensp() == null || request.getTensp().trim().isEmpty()) {
                return ApiResponse.error("Tên sản phẩm không được để trống");
            }

            if (request.getGia() == null || request.getGia() <= 0) {
                return ApiResponse.error("Giá sản phẩm phải lớn hơn 0");
            }

            if (request.getCategoryId() == null) {
                return ApiResponse.error("Category ID không được để trống");
            }

            // Lấy product từ database
            Product product = productRepository.findById(productId).get();
            
            // Cập nhật thông tin
            product.setTensp(request.getTensp().trim());
            product.setGia(request.getGia());
            product.setCategory_id(request.getCategoryId());
            product.setHinh(request.getHinh() != null ? request.getHinh().trim() : "");

            // Lưu vào database
            Product updatedProduct = productRepository.save(product);

            // Convert thành response DTO
            ProductResponse response = ProductResponse.fromProduct(updatedProduct);

            return ApiResponse.success("Cập nhật sản phẩm thành công", response);

        } catch (Exception e) {
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Xóa sản phẩm (chỉ ADMIN)
    public ApiResponse<String> deleteProduct(Integer productId) {
        try {
            // Kiểm tra sản phẩm có tồn tại không
            if (!productRepository.existsById(productId)) {
                return ApiResponse.error("Sản phẩm không tồn tại với ID: " + productId);
            }

            // Xóa sản phẩm
            productRepository.deleteById(productId);

            return ApiResponse.success("Xóa sản phẩm thành công");

        } catch (Exception e) {
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Lấy thông tin sản phẩm theo ID
    public ApiResponse<ProductResponse> getProductById(Integer productId) {
        try {
            if (!productRepository.existsById(productId)) {
                return ApiResponse.error("Sản phẩm không tồn tại với ID: " + productId);
            }

            Product product = productRepository.findById(productId).get();
            ProductResponse response = ProductResponse.fromProduct(product);

            return ApiResponse.success("Lấy thông tin sản phẩm thành công", response);

        } catch (Exception e) {
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
}
