package SpringBoot.demo.Service;

import SpringBoot.demo.Enum.SortType;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Service chuyên xử lý logic sắp xếp sản phẩm
 * Tuân thủ nguyên lý Single Responsibility: chỉ quản lý việc sắp xếp
 */
@Service
public class ProductSortService {
    
    /**
     * Tạo Sort object từ SortType
     * @param sortType loại sắp xếp
     * @return Sort object cho Spring Data
     */
    public Sort createSort(SortType sortType) {
        switch (sortType) {
            case POPULAR:
                // Giả định sản phẩm phổ biến là sản phẩm có ID thấp (được tạo trước)
                // Trong thực tế có thể dựa vào số lượng bán, lượt xem, rating, etc.
                return Sort.by(Sort.Direction.ASC, "id");
                
            case NEWEST:
                return Sort.by(Sort.Direction.DESC, "id");
                
            case PRICE_ASC:
                return Sort.by(Sort.Direction.ASC, "gia");
                
            case PRICE_DESC:
                return Sort.by(Sort.Direction.DESC, "gia");
                
            case NAME_ASC:
                return Sort.by(Sort.Direction.ASC, "tensp");
                
            case NAME_DESC:
                return Sort.by(Sort.Direction.DESC, "tensp");
                
            default:
                return Sort.by(Sort.Direction.DESC, "id"); // Default: newest first
        }
    }
    
    /**
     * Tạo Sort với nhiều tiêu chí
     * @param primarySort sắp xếp chính
     * @param secondarySort sắp xếp phụ (fallback)
     * @return Sort object kết hợp
     */
    public Sort createMultiSort(SortType primarySort, SortType secondarySort) {
        Sort primary = createSort(primarySort);
        Sort secondary = createSort(secondarySort);
        return primary.and(secondary);
    }
    
    /**
     * Tạo Sort mặc định với fallback
     * Sắp xếp chính + fallback theo ID giảm dần để đảm bảo thứ tự nhất quán
     */
    public Sort createSortWithFallback(SortType sortType) {
        Sort primarySort = createSort(sortType);
        
        // Nếu không phải sắp xếp theo ID, thêm ID làm tiêu chí phụ
        if (sortType != SortType.NEWEST && sortType != SortType.POPULAR) {
            Sort fallbackSort = Sort.by(Sort.Direction.DESC, "id");
            return primarySort.and(fallbackSort);
        }
        
        return primarySort;
    }
    
    /**
     * Kiểm tra xem có phải sắp xếp theo giá không
     */
    public boolean isPriceSorting(SortType sortType) {
        return sortType == SortType.PRICE_ASC || sortType == SortType.PRICE_DESC;
    }
    
    /**
     * Kiểm tra xem có phải sắp xếp theo tên không
     */
    public boolean isNameSorting(SortType sortType) {
        return sortType == SortType.NAME_ASC || sortType == SortType.NAME_DESC;
    }
    
    /**
     * Lấy hướng sắp xếp từ SortType
     */
    public Sort.Direction getSortDirection(SortType sortType) {
        switch (sortType) {
            case PRICE_DESC:
            case NAME_DESC:
            case NEWEST:
                return Sort.Direction.DESC;
            case PRICE_ASC:
            case NAME_ASC:
            case POPULAR:
            default:
                return Sort.Direction.ASC;
        }
    }
}
