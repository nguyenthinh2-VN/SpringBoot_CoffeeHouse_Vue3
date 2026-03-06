package SpringBoot.demo.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 100, message = "Tên sản phẩm phải từ 2-100 ký tự")
    private String tensp;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private Double gia;

    @NotNull(message = "Category ID không được để trống")
    private Integer categoryId;

    @Size(max = 255, message = "URL hình ảnh không được quá 255 ký tự")
    private String hinh;

    // Constructors
    public ProductRequest() {}

    public ProductRequest(String tensp, Double gia, Integer categoryId, String hinh) {
        this.tensp = tensp;
        this.gia = gia;
        this.categoryId = categoryId;
        this.hinh = hinh;
    }
}
