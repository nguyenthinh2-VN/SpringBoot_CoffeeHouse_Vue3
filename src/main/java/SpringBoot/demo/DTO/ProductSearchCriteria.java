package SpringBoot.demo.DTO;

import SpringBoot.demo.Enum.SortType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa các tiêu chí tìm kiếm và lọc sản phẩm
 * Tuân thủ nguyên lý Single Responsibility: chỉ chứa dữ liệu tìm kiếm
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {
    
    /**
     * Từ khóa tìm kiếm full-text
     */
    private String keyword;
    
    /**
     * ID danh mục để lọc
     */
    private Integer categoryId;
    
    /**
     * Giá tối thiểu
     */
    private Double minPrice;
    
    /**
     * Giá tối đa
     */
    private Double maxPrice;
    
    /**
     * Loại sắp xếp
     */
    private SortType sortType = SortType.NEWEST;
    
    /**
     * Số trang (bắt đầu từ 0)
     */
    private int page = 0;
    
    /**
     * Số lượng item trên mỗi trang
     */
    private int size = 10;
    
    /**
     * Constructor với các tham số cơ bản
     */
    public ProductSearchCriteria(String keyword, Integer categoryId, String sortType) {
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.sortType = SortType.fromValue(sortType);
    }
    
    /**
     * Kiểm tra có tiêu chí tìm kiếm nào không
     */
    public boolean hasSearchCriteria() {
        return (keyword != null && !keyword.trim().isEmpty()) ||
               categoryId != null ||
               minPrice != null ||
               maxPrice != null;
    }
    
    /**
     * Kiểm tra có lọc theo giá không
     */
    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }
    
    /**
     * Lấy từ khóa đã được trim và lowercase
     */
    public String getCleanKeyword() {
        return keyword != null ? keyword.trim().toLowerCase() : null;
    }
}
