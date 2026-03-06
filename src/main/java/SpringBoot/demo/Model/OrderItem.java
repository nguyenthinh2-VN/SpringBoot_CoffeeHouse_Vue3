package SpringBoot.demo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "order_items")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order ID không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Product ID không được để trống")
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "product_name")
    private String productName;

    @NotNull(message = "Size ID không được để trống")
    @Column(name = "size_id", nullable = false)
    private Integer sizeId;

    @Column(name = "size_name")
    private String sizeName;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Giá không được để trống")
    @Column(name = "price", nullable = false)
    private BigDecimal price; // Giá sản phẩm

    @Column(name = "size_price")
    private BigDecimal sizePrice = BigDecimal.ZERO; // Giá size

    @Column(name = "topping_ids")
    private String toppingIds; // JSON array: [1,2,3]

    @Column(name = "topping_names")
    private String toppingNames; // Tên topping để hiển thị

    @Column(name = "topping_price")
    private BigDecimal toppingPrice = BigDecimal.ZERO;

    @Column(name = "ice_id")
    private Integer iceId;

    @Column(name = "ice_option_id")
    private Integer iceOptionId;

    @Column(name = "ice_option_name")
    private String iceOptionName;

    @Column(name = "notes")
    private String notes;

    // Constructors
    public OrderItem(Order order, Integer productId, String productName, 
                     Integer sizeId, String sizeName, Integer quantity, BigDecimal price) {
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.quantity = quantity;
        this.price = price;
    }
}
