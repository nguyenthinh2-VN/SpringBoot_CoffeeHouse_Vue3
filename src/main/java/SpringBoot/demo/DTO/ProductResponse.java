package SpringBoot.demo.DTO;

import SpringBoot.demo.Model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductResponse {
    
    private Integer id;
    private String tensp;
    private Double gia;
    private Integer categoryId;
    private String hinh;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public ProductResponse(Integer id, String tensp, Double gia, Integer categoryId, String hinh) {
        this.id = id;
        this.tensp = tensp;
        this.gia = gia;
        this.categoryId = categoryId;
        this.hinh = hinh;
    }

    // Static method để convert từ Product entity
    public static ProductResponse fromProduct(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setTensp(product.getTensp());
        response.setGia(product.getGia());
        response.setCategoryId(product.getCategory_id());
        response.setHinh(product.getHinh());
        return response;
    }
}
