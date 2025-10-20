package SpringBoot.demo.DTO;

import SpringBoot.demo.Model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO cho response chi tiết sản phẩm với các tùy chọn theo category
 * Tuân thủ nguyên lý Single Responsibility: chỉ chứa dữ liệu response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    
    /**
     * Thông tin sản phẩm cơ bản
     */
    private Product product;
    
    /**
     * Danh sách tùy chọn đá (cho cả cà phê và trà sữa)
     */
    private List<IceOptionDTO> iceOptions;
    
    /**
     * Danh sách size (chỉ cho trà sữa - category 2)
     */
    private List<SizeDTO> sizes;
    
    /**
     * Danh sách topping (chỉ cho trà sữa - category 2)
     */
    private List<ToppingDTO> toppings;
    
    /**
     * Thông tin category để frontend biết hiển thị options nào
     */
    private CategoryInfo categoryInfo;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IceOptionDTO {
        private int id;
        private String tenice;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeDTO {
        private int id;
        private String tensize;
        private double gia;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingDTO {
        private int id;
        private String tentopping;
        private double gia;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private int categoryId;
        private String categoryName;
        private boolean hasIceOptions;
        private boolean hasSizes;
        private boolean hasToppings;
    }
}
