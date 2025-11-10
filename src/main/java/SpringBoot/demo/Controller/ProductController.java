package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.DTO.ProductSearchCriteria;
import SpringBoot.demo.DTO.ProductDetailResponse;
import SpringBoot.demo.Model.Product;
import SpringBoot.demo.Model.PaginationResponse;
import SpringBoot.demo.Repository.Product.ProductRepository;
import SpringBoot.demo.Service.Product.ProductSearchService;
import SpringBoot.demo.Service.Product.ProductDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@Tag(name = "Product Public", description = "APIs công khai cho sản phẩm (không cần đăng nhập)")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductSearchService productSearchService;
    
    @Autowired
    private ProductDetailService productDetailService;

    // GET /products - Lấy tất cả sản phẩm với phân trang (công khai)
    @Operation(summary = "Lấy danh sách sản phẩm", description = "Lấy tất cả sản phẩm với phân trang (không cần đăng nhập)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PaginationResponse<Product>>> getAllProducts(
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "Số lượng item trên mỗi trang") @RequestParam(defaultValue = "10") Integer size) {
        
        try {
            // Convert page from 1-based to 0-based for Spring Data
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Product> productPage = productRepository.findAll(pageable);
            
            // Create pagination info
            PaginationResponse.PaginationInfo paginationInfo = new PaginationResponse.PaginationInfo(
                    productPage.getTotalElements(),
                    productPage.getNumberOfElements(),
                    page,
                    productPage.getTotalPages(),
                    0 // updateToday - có thể implement sau
            );
            
            PaginationResponse<Product> result = new PaginationResponse<>(productPage.getContent(), paginationInfo);
            ApiResponse<PaginationResponse<Product>> response = ApiResponse.success(result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<PaginationResponse<Product>> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // GET /products/search - Tìm kiếm sản phẩm công khai
    @Operation(summary = "Tìm kiếm sản phẩm", description = "Tìm kiếm và lọc sản phẩm với phân trang (không cần đăng nhập)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công")
    })
    @GetMapping("/products/search")
    public ResponseEntity<ApiResponse<PaginationResponse<Product>>> searchProducts(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID danh mục") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "Giá tối thiểu") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Giá tối đa") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Loại sắp xếp: popular, newest, price_asc, price_desc, name_asc, name_desc") 
            @RequestParam(defaultValue = "newest") String sortType,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng item trên mỗi trang") @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Tạo criteria tìm kiếm
            ProductSearchCriteria criteria = new ProductSearchCriteria();
            criteria.setKeyword(keyword);
            criteria.setCategoryId(categoryId);
            criteria.setMinPrice(minPrice);
            criteria.setMaxPrice(maxPrice);
            criteria.setSortType(SpringBoot.demo.Enum.SortType.fromValue(sortType));
            criteria.setPage(page - 1); // Convert to 0-based
            criteria.setSize(size);

            // Thực hiện tìm kiếm
            PaginationResponse<Product> result = productSearchService.searchProducts(criteria);
            
            // Adjust page number back to 1-based for response
            result.getPagination().setCurrentPage(page);
            
            ApiResponse<PaginationResponse<Product>> response = ApiResponse.success(result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<PaginationResponse<Product>> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // GET /products/category/{categoryId} - Lấy sản phẩm theo danh mục
    @Operation(summary = "Lấy sản phẩm theo danh mục", description = "Lấy sản phẩm theo danh mục với phân trang và sắp xếp")
    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<ApiResponse<PaginationResponse<Product>>> getProductsByCategory(
            @Parameter(description = "ID danh mục") @PathVariable Integer categoryId,
            @Parameter(description = "Loại sắp xếp") @RequestParam(defaultValue = "newest") String sortType,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng item trên mỗi trang") @RequestParam(defaultValue = "10") int size) {
        
        try {
            PaginationResponse<Product> result = productSearchService.getProductsByCategory(
                categoryId, page - 1, size, sortType);
            
            // Adjust page number back to 1-based for response
            result.getPagination().setCurrentPage(page);
            
            ApiResponse<PaginationResponse<Product>> response = ApiResponse.success(result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<PaginationResponse<Product>> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // GET /products/newest - Lấy sản phẩm mới nhất
    @Operation(summary = "Lấy sản phẩm mới nhất", description = "Lấy danh sách sản phẩm mới nhất")
    @GetMapping("/products/newest")
    public ResponseEntity<ApiResponse<PaginationResponse<Product>>> getNewestProducts(
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng item trên mỗi trang") @RequestParam(defaultValue = "10") int size) {
        
        try {
            PaginationResponse<Product> result = productSearchService.getNewestProducts(page - 1, size);
            
            // Adjust page number back to 1-based for response
            result.getPagination().setCurrentPage(page);
            
            ApiResponse<PaginationResponse<Product>> response = ApiResponse.success(result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<PaginationResponse<Product>> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // GET /products/{id} - Lấy chi tiết sản phẩm với các tùy chọn
    @Operation(summary = "Lấy chi tiết sản phẩm", description = "Lấy thông tin chi tiết sản phẩm với các tùy chọn theo category")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    })
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(
            @Parameter(description = "ID sản phẩm") @PathVariable Integer id) {
        
        try {
            ProductDetailResponse productDetail = productDetailService.getProductDetail(id);
            
            if (productDetail != null) {
                ApiResponse<ProductDetailResponse> response = ApiResponse.success(productDetail);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<ProductDetailResponse> response = ApiResponse.error("Không tìm thấy sản phẩm với ID: " + id);
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<ProductDetailResponse> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // GET /products/{id}/basic - Lấy thông tin cơ bản sản phẩm (không có options)
    @Operation(summary = "Lấy thông tin cơ bản sản phẩm", description = "Lấy thông tin cơ bản của sản phẩm (không bao gồm options)")
    @GetMapping("/products/{id}/basic")
    public ResponseEntity<ApiResponse<Product>> getProductBasicInfo(
            @Parameter(description = "ID sản phẩm") @PathVariable Integer id) {
        
        try {
            Product product = productRepository.findById(id).orElse(null);
            
            if (product != null) {
                ApiResponse<Product> response = ApiResponse.success(product);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Product> response = ApiResponse.error("Không tìm thấy sản phẩm với ID: " + id);
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            ApiResponse<Product> errorResponse = ApiResponse.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
