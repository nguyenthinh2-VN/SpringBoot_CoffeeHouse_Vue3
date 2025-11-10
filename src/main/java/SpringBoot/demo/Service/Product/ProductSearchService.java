package SpringBoot.demo.Service.Product;

import SpringBoot.demo.DTO.ProductSearchCriteria;
import SpringBoot.demo.Model.PaginationResponse;
import SpringBoot.demo.Model.Product;
import SpringBoot.demo.Repository.Product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Service chuyên xử lý logic tìm kiếm sản phẩm
 * Tuân thủ nguyên lý Single Responsibility: chỉ quản lý việc tìm kiếm và lọc
 */
@Service
@RequiredArgsConstructor
public class ProductSearchService {
    
    private final ProductRepository productRepository;
    private final ProductSortService productSortService;
    
    /**
     * Tìm kiếm sản phẩm với các tiêu chí và phân trang
     * @param criteria tiêu chí tìm kiếm
     * @return kết quả phân trang
     */
    public PaginationResponse<Product> searchProducts(ProductSearchCriteria criteria) {
        // Tạo Pageable với sắp xếp
        Sort sort = productSortService.createSortWithFallback(criteria.getSortType());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        
        // Thực hiện tìm kiếm dựa trên tiêu chí
        Page<Product> productPage = executeSearch(criteria, pageable);
        
        // Chuyển đổi sang PaginationResponse
        return convertToPaginationResponse(productPage);
    }
    
    /**
     * Thực hiện tìm kiếm dựa trên tiêu chí cụ thể
     */
    private Page<Product> executeSearch(ProductSearchCriteria criteria, Pageable pageable) {
        String keyword = criteria.getCleanKeyword();
        Integer categoryId = criteria.getCategoryId();
        Double minPrice = criteria.getMinPrice();
        Double maxPrice = criteria.getMaxPrice();
        
        // Nếu không có tiêu chí tìm kiếm, trả về tất cả
        if (!criteria.hasSearchCriteria()) {
            return productRepository.findAll(pageable);
        }
        
        // Tìm kiếm với tất cả tiêu chí
        if (keyword != null && categoryId != null && criteria.hasPriceFilter()) {
            return productRepository.findByAllCriteria(keyword, categoryId, minPrice, maxPrice, pageable);
        }
        
        // Tìm kiếm với từ khóa + danh mục
        if (keyword != null && categoryId != null) {
            return productRepository.findByKeywordAndCategory(keyword, categoryId, pageable);
        }
        
        // Tìm kiếm với từ khóa + khoảng giá
        if (keyword != null && criteria.hasPriceFilter()) {
            return productRepository.findByKeywordAndPriceRange(keyword, minPrice, maxPrice, pageable);
        }
        
        // Tìm kiếm với danh mục + khoảng giá
        if (categoryId != null && criteria.hasPriceFilter()) {
            return productRepository.findByCategoryAndPriceRange(categoryId, minPrice, maxPrice, pageable);
        }
        
        // Tìm kiếm chỉ với từ khóa
        if (keyword != null) {
            return productRepository.findByKeyword(keyword, pageable);
        }
        
        // Tìm kiếm chỉ với danh mục
        if (categoryId != null) {
            return productRepository.findByCategory_id(categoryId, pageable);
        }
        
        // Tìm kiếm chỉ với khoảng giá
        if (criteria.hasPriceFilter()) {
            return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        }
        
        // Fallback: trả về tất cả
        return productRepository.findAll(pageable);
    }
    
    /**
     * Chuyển đổi Page<Product> sang PaginationResponse<Product>
     */
    private PaginationResponse<Product> convertToPaginationResponse(Page<Product> productPage) {
        PaginationResponse.PaginationInfo paginationInfo = new PaginationResponse.PaginationInfo(
            productPage.getTotalElements(),
            productPage.getSize(),
            productPage.getNumber(),
            productPage.getTotalPages(),
            0 // updateToday - có thể implement sau
        );
        
        return new PaginationResponse<>(productPage.getContent(), paginationInfo);
    }
    
    /**
     * Tìm kiếm sản phẩm mới nhất
     */
    public PaginationResponse<Product> getNewestProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> productPage = productRepository.findAllByOrderByIdDesc(pageable);
        return convertToPaginationResponse(productPage);
    }
    
    /**
     * Tìm kiếm sản phẩm theo danh mục với phân trang
     */
    public PaginationResponse<Product> getProductsByCategory(Integer categoryId, int page, int size, String sortType) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setCategoryId(categoryId);
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setSortType(SpringBoot.demo.Enum.SortType.fromValue(sortType));
        
        return searchProducts(criteria);
    }
    
    /**
     * Đếm số lượng sản phẩm theo tiêu chí tìm kiếm
     */
    public long countProductsByCriteria(ProductSearchCriteria criteria) {
        // Tạo pageable với size = 1 chỉ để đếm
        Pageable pageable = PageRequest.of(0, 1);
        Page<Product> productPage = executeSearch(criteria, pageable);
        return productPage.getTotalElements();
    }
    
    /**
     * Kiểm tra xem có sản phẩm nào thỏa mãn tiêu chí không
     */
    public boolean hasProductsMatchingCriteria(ProductSearchCriteria criteria) {
        return countProductsByCriteria(criteria) > 0;
    }
}
