package SpringBoot.demo.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull(message = "Product ID không được để trống")
    private Integer productId;

    @NotNull(message = "Size ID không được để trống")
    private Integer sizeId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    private List<Integer> toppingIds; // Optional

    private Integer iceOptionId; // Optional

    private String notes; // Optional
}
