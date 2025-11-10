package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.DTO.ProductRequest;
import SpringBoot.demo.DTO.ProductResponse;
import SpringBoot.demo.DTO.ProductSearchCriteria;
import SpringBoot.demo.Model.PaginationResponse;
import SpringBoot.demo.Model.Product;
import SpringBoot.demo.Model.User;
import SpringBoot.demo.Service.Admin.AdminService;
import SpringBoot.demo.Service.Product.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Management", description = "APIs quản lý dành cho Admin (cần quyền ADMIN)")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ProductSearchService productSearchService;

    // POST /admin/products - Thêm sản phẩm mới (chỉ ADMIN)
    @Operation(summary = "Thêm sản phẩm mới", description = "Tạo sản phẩm mới (chỉ Admin)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Thêm sản phẩm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(@Valid @RequestBody ProductRequest request) {
        try {
            // Kiểm tra user có role ADMIN không
            if (!isUserAdmin()) {
                ApiResponse<ProductResponse> errorResponse = ApiResponse.error("Bạn không có quyền thực hiện thao tác này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            ApiResponse<ProductResponse> response = adminService.addProduct(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<ProductResponse> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // PUT /admin/products/{id} - Cập nhật sản phẩm (chỉ ADMIN)
    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Integer id, 
            @Valid @RequestBody ProductRequest request) {
        try {
            // Kiểm tra user có role ADMIN không
            if (!isUserAdmin()) {
                ApiResponse<ProductResponse> errorResponse = ApiResponse.error("Bạn không có quyền thực hiện thao tác này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            ApiResponse<ProductResponse> response = adminService.updateProduct(id, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<ProductResponse> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // DELETE /admin/products/{id} - Xóa sản phẩm (chỉ ADMIN)
    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Integer id) {
        try {
            // Kiểm tra user có role ADMIN không
            if (!isUserAdmin()) {
                ApiResponse<String> errorResponse = ApiResponse.error("Bạn không có quyền thực hiện thao tác này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            ApiResponse<String> response = adminService.deleteProduct(id);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /admin/products/{id} - Lấy thông tin sản phẩm (chỉ ADMIN)
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Integer id) {
        try {
            // Kiểm tra user có role ADMIN không
            if (!isUserAdmin()) {
                ApiResponse<ProductResponse> errorResponse = ApiResponse.error("Bạn không có quyền thực hiện thao tác này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            ApiResponse<ProductResponse> response = adminService.getProductById(id);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<ProductResponse> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /admin/products - Tìm kiếm và lọc sản phẩm với phân trang (chỉ ADMIN)
    @Operation(summary = "Tìm kiếm sản phẩm", description = "Tìm kiếm và lọc sản phẩm với phân trang (chỉ Admin)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PaginationResponse<Product>>> searchProducts(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID danh mục") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "Giá tối thiểu") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Giá tối đa") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Loại sắp xếp: popular, newest, price_asc, price_desc, name_asc, name_desc") 
            @RequestParam(defaultValue = "newest") String sortType,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item trên mỗi trang") @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Kiểm tra user có role ADMIN không
            if (!isUserAdmin()) {
                ApiResponse<PaginationResponse<Product>> errorResponse = ApiResponse.error("Bạn không có quyền thực hiện thao tác này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Tạo criteria tìm kiếm
            ProductSearchCriteria criteria = new ProductSearchCriteria();
            criteria.setKeyword(keyword);
            criteria.setCategoryId(categoryId);
            criteria.setMinPrice(minPrice);
            criteria.setMaxPrice(maxPrice);
            criteria.setSortType(SpringBoot.demo.Enum.SortType.fromValue(sortType));
            criteria.setPage(page);
            criteria.setSize(size);

            // Thực hiện tìm kiếm
            PaginationResponse<Product> result = productSearchService.searchProducts(criteria);
            ApiResponse<PaginationResponse<Product>> response = ApiResponse.success(result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<PaginationResponse<Product>> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /admin/products/search - Tìm kiếm nâng cao (chỉ ADMIN)
    @Operation(summary = "Tìm kiếm sản phẩm nâng cao", description = "Tìm kiếm sản phẩm với body request (chỉ Admin)")
    @PostMapping("/products/search")
    public ResponseEntity<ApiResponse<PaginationResponse<Product>>> advancedSearchProducts(
            @Valid @RequestBody ProductSearchCriteria criteria) {
        
        try {
            // Kiểm tra user có role ADMIN không
            if (!isUserAdmin()) {
                ApiResponse<PaginationResponse<Product>> errorResponse = ApiResponse.error("Bạn không có quyền thực hiện thao tác này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Thực hiện tìm kiếm
            PaginationResponse<Product> result = productSearchService.searchProducts(criteria);
            ApiResponse<PaginationResponse<Product>> response = ApiResponse.success(result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<PaginationResponse<Product>> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /admin/dashboard - Dashboard info (chỉ ADMIN)
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        try {
            // Kiểm tra user có role ADMIN không
            if (!isUserAdmin()) {
                ApiResponse<String> errorResponse = ApiResponse.error("Bạn không có quyền truy cập trang này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Lấy thông tin admin từ Security Context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User admin = (User) authentication.getPrincipal();

            String dashboardInfo = String.format(
                "Chào mừng Admin %s! Bạn đang truy cập trang quản trị.", 
                admin.getUsername()
            );

            ApiResponse<String> response = ApiResponse.success(dashboardInfo);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper method để kiểm tra user có role ADMIN không
    private boolean isUserAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            User user = (User) authentication.getPrincipal();
            return user.getRole().name().equals("ADMIN");
            
        } catch (Exception e) {
            return false;
        }
    }
}
