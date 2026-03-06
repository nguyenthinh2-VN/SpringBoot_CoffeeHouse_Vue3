package SpringBoot.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;

    private Integer productId;

    private String productName;

    private Integer sizeId;

    private String sizeName;

    private Integer quantity;

    private BigDecimal price; // Giá sản phẩm

    private BigDecimal sizePrice; // Giá size

    private String toppingIds;

    private String toppingNames;

    private BigDecimal toppingPrice; // Giá topping

    private Integer iceOptionId;

    private String iceOptionName;

    private String notes;

    private BigDecimal subtotal; // (price + sizePrice + toppingPrice) * quantity
}
