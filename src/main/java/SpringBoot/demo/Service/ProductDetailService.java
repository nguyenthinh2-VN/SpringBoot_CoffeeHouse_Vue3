package SpringBoot.demo.Service;

import SpringBoot.demo.DTO.ProductDetailResponse;
import SpringBoot.demo.Model.*;
import SpringBoot.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service chuyên xử lý logic chi tiết sản phẩm với các tùy chọn theo category
 * Tuân thủ nguyên lý Single Responsibility: chỉ quản lý logic chi tiết sản phẩm
 */
@Service
@RequiredArgsConstructor
public class ProductDetailService {
    
    private final ProductRepository productRepository;
    private final IceOptionRepository iceOptionRepository;
    private final SizeRepository sizeRepository;
    private final ToppingRepository toppingRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * Lấy chi tiết sản phẩm với các tùy chọn dựa trên category
     */
    public ProductDetailResponse getProductDetail(Integer productId) {
        // Lấy thông tin sản phẩm
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return null;
        }
        
        Product product = productOpt.get();
        int categoryId = product.getCategory_id();
        
        // Tạo response
        ProductDetailResponse response = new ProductDetailResponse();
        response.setProduct(product);
        
        // Lấy thông tin category
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        String categoryName = categoryOpt.map(Category::getName).orElse("Unknown");
        
        // Xử lý logic theo category
        if (categoryId == 1) {
            // Category 1: Cà phê - chỉ có tùy chọn đá
            setupCoffeeOptions(response, categoryName);
        } else if (categoryId == 2) {
            // Category 2: Trà sữa - có size, đá, và topping
            setupMilkTeaOptions(response, categoryName);
        } else {
            // Category khác - không có tùy chọn đặc biệt
            setupDefaultOptions(response, categoryName);
        }
        
        return response;
    }
    
    /**
     * Thiết lập tùy chọn cho cà phê (category 1)
     */
    private void setupCoffeeOptions(ProductDetailResponse response, String categoryName) {
        // Chỉ có tùy chọn đá
        List<IceOption> iceOptions = iceOptionRepository.findAll();
        response.setIceOptions(convertToIceOptionDTOs(iceOptions));
        
        // Không có size và topping
        response.setSizes(null);
        response.setToppings(null);
        
        // Thiết lập category info
        ProductDetailResponse.CategoryInfo categoryInfo = new ProductDetailResponse.CategoryInfo(
            1, categoryName, true, false, false
        );
        response.setCategoryInfo(categoryInfo);
    }
    
    /**
     * Thiết lập tùy chọn cho trà sữa (category 2)
     */
    private void setupMilkTeaOptions(ProductDetailResponse response, String categoryName) {
        // Có tất cả các tùy chọn
        List<IceOption> iceOptions = iceOptionRepository.findAll();
        List<Size> sizes = sizeRepository.findAll();
        List<Topping> toppings = toppingRepository.findAll();
        
        response.setIceOptions(convertToIceOptionDTOs(iceOptions));
        response.setSizes(convertToSizeDTOs(sizes));
        response.setToppings(convertToToppingDTOs(toppings));
        
        // Thiết lập category info
        ProductDetailResponse.CategoryInfo categoryInfo = new ProductDetailResponse.CategoryInfo(
            2, categoryName, true, true, true
        );
        response.setCategoryInfo(categoryInfo);
    }
    
    /**
     * Thiết lập tùy chọn mặc định cho category khác
     */
    private void setupDefaultOptions(ProductDetailResponse response, String categoryName) {
        // Không có tùy chọn đặc biệt
        response.setIceOptions(null);
        response.setSizes(null);
        response.setToppings(null);
        
        // Thiết lập category info
        ProductDetailResponse.CategoryInfo categoryInfo = new ProductDetailResponse.CategoryInfo(
            response.getProduct().getCategory_id(), categoryName, false, false, false
        );
        response.setCategoryInfo(categoryInfo);
    }
    
    /**
     * Convert IceOption entities to DTOs
     */
    private List<ProductDetailResponse.IceOptionDTO> convertToIceOptionDTOs(List<IceOption> iceOptions) {
        return iceOptions.stream()
            .map(ice -> new ProductDetailResponse.IceOptionDTO(ice.getId(), ice.getTen_ice()))
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Size entities to DTOs
     */
    private List<ProductDetailResponse.SizeDTO> convertToSizeDTOs(List<Size> sizes) {
        return sizes.stream()
            .map(size -> new ProductDetailResponse.SizeDTO(size.getId(), size.getTensize(), size.getGia()))
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Topping entities to DTOs
     */
    private List<ProductDetailResponse.ToppingDTO> convertToToppingDTOs(List<Topping> toppings) {
        return toppings.stream()
            .map(topping -> new ProductDetailResponse.ToppingDTO(topping.getId(), topping.getTentopping(), topping.getGia()))
            .collect(Collectors.toList());
    }
    
    /**
     * Kiểm tra xem sản phẩm có tồn tại không
     */
    public boolean isProductExists(Integer productId) {
        return productRepository.existsById(productId);
    }
    
    /**
     * Lấy tất cả tùy chọn đá (dùng chung cho cả hai category)
     */
    public List<ProductDetailResponse.IceOptionDTO> getAllIceOptions() {
        List<IceOption> iceOptions = iceOptionRepository.findAll();
        return convertToIceOptionDTOs(iceOptions);
    }
    
    /**
     * Lấy tất cả size (chỉ cho trà sữa)
     */
    public List<ProductDetailResponse.SizeDTO> getAllSizes() {
        List<Size> sizes = sizeRepository.findAll();
        return convertToSizeDTOs(sizes);
    }
    
    /**
     * Lấy tất cả topping (chỉ cho trà sữa)
     */
    public List<ProductDetailResponse.ToppingDTO> getAllToppings() {
        List<Topping> toppings = toppingRepository.findAll();
        return convertToToppingDTOs(toppings);
    }
}
