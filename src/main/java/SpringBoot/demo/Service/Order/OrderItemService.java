package SpringBoot.demo.Service.Order;

import SpringBoot.demo.DTO.OrderItemRequest;
import SpringBoot.demo.Model.*;
import SpringBoot.demo.Repository.Option.IceOptionRepository;
import SpringBoot.demo.Repository.Option.SizeRepository;
import SpringBoot.demo.Repository.Option.ToppingRepository;
import SpringBoot.demo.Repository.Product.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service xử lý order items
 * Trách nhiệm: Validate sản phẩm, tính giá item, tạo OrderItem
 */
@Slf4j
@Service
public class OrderItemService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ToppingRepository toppingRepository;

    @Autowired
    private IceOptionRepository iceOptionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lấy thông tin item chi tiết (dùng cho preview)
     * SRP: Chỉ lấy thông tin, không tạo OrderItem
     */
    public ItemDetailResult getItemDetails(OrderItemRequest itemRequest) {
        try {
            log.info("Getting item details: productId={}, sizeId={}", 
                itemRequest.getProductId(), itemRequest.getSizeId());
            
            // Get product
            Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
            if (productOpt.isEmpty()) {
                throw new RuntimeException("Sản phẩm không tồn tại");
            }
            Product product = productOpt.get();
            
            // Get size
            Optional<Size> sizeOpt = sizeRepository.findById(itemRequest.getSizeId());
            if (sizeOpt.isEmpty()) {
                throw new RuntimeException("Size không tồn tại");
            }
            Size size = sizeOpt.get();
            
            // Get toppings
            String toppingNames = "";
            BigDecimal toppingPrice = BigDecimal.ZERO;
            if (itemRequest.getToppingIds() != null && !itemRequest.getToppingIds().isEmpty()) {
                List<String> names = new ArrayList<>();
                for (Integer toppingId : itemRequest.getToppingIds()) {
                    Optional<Topping> toppingOpt = toppingRepository.findById(toppingId);
                    if (toppingOpt.isPresent()) {
                        names.add(toppingOpt.get().getTentopping());
                        toppingPrice = toppingPrice.add(new BigDecimal(toppingOpt.get().getGia()));
                    }
                }
                toppingNames = String.join(", ", names);
            }
            
            // Get ice option
            String iceOptionName = "";
            if (itemRequest.getIceOptionId() != null && itemRequest.getIceOptionId() > 0) {
                Optional<IceOption> iceOpt = iceOptionRepository.findById(itemRequest.getIceOptionId());
                if (iceOpt.isPresent()) {
                    iceOptionName = iceOpt.get().getTen_ice();
                }
            }
            
            return new ItemDetailResult(
                product.getTensp(),
                size.getTensize(),
                toppingNames,
                iceOptionName,
                new BigDecimal(product.getGia()),
                new BigDecimal(size.getGia()),
                toppingPrice
            );
        } catch (Exception e) {
            log.error("Error getting item details: {}", e.getMessage());
            throw new RuntimeException("Lỗi lấy thông tin sản phẩm: " + e.getMessage());
        }
    }
    
    /**
     * Tính giá item (dùng cho preview)
     * SRP: Chỉ tính giá, không tạo OrderItem
     */
    public BigDecimal calculateItemPrice(OrderItemRequest itemRequest) {
        try {
            log.info("Calculating item price: productId={}, sizeId={}", 
                itemRequest.getProductId(), itemRequest.getSizeId());
            
            // Validate product
            Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
            if (productOpt.isEmpty()) {
                log.error("Product not found: {}", itemRequest.getProductId());
                throw new RuntimeException("Sản phẩm ID " + itemRequest.getProductId() + " không tồn tại");
            }
            Product product = productOpt.get();
            
            // Validate size
            Optional<Size> sizeOpt = sizeRepository.findById(itemRequest.getSizeId());
            if (sizeOpt.isEmpty()) {
                log.error("Size not found: {}", itemRequest.getSizeId());
                throw new RuntimeException("Size ID " + itemRequest.getSizeId() + " không tồn tại");
            }
            Size size = sizeOpt.get();
            
            // Calculate topping price
            BigDecimal toppingPrice = BigDecimal.ZERO;
            if (itemRequest.getToppingIds() != null && !itemRequest.getToppingIds().isEmpty()) {
                for (Integer toppingId : itemRequest.getToppingIds()) {
                    Optional<Topping> toppingOpt = toppingRepository.findById(toppingId);
                    if (toppingOpt.isPresent()) {
                        toppingPrice = toppingPrice.add(new BigDecimal(toppingOpt.get().getGia()));
                    }
                }
            }
            
            // Calculate item price = (product price + size price + topping price)
            BigDecimal itemPrice = new BigDecimal(product.getGia())
                    .add(new BigDecimal(size.getGia()))
                    .add(toppingPrice);
            
            log.info("Item price: {} (product: {}, size: {}, topping: {})", 
                     itemPrice, product.getGia(), size.getGia(), toppingPrice);
            
            return itemPrice;
        } catch (Exception e) {
            log.error("Error calculating item price: {}", e.getMessage());
            throw new RuntimeException("Lỗi tính giá sản phẩm: " + e.getMessage());
        }
    }
    
    /**
     * Xử lý một item trong đơn hàng
     * @return ItemProcessResult chứa OrderItem và itemPrice
     */
    public ItemProcessResult processOrderItem(OrderItemRequest itemRequest, Order order) {
        try {
            log.info("Processing item: productId={}, sizeId={}, quantity={}", 
                itemRequest.getProductId(), itemRequest.getSizeId(), itemRequest.getQuantity());

            // Validate product
            Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
            if (productOpt.isEmpty()) {
                return new ItemProcessResult(false, "Sản phẩm không tồn tại", null, BigDecimal.ZERO);
            }
            Product product = productOpt.get();
            log.info("Product found: {}", product.getTensp());

            // Validate size
            Optional<Size> sizeOpt = sizeRepository.findById(itemRequest.getSizeId());
            if (sizeOpt.isEmpty()) {
                return new ItemProcessResult(false, "Size không tồn tại", null, BigDecimal.ZERO);
            }
            Size size = sizeOpt.get();
            log.info("Size found: {}", size.getTensize());

            // Tính giá size
            BigDecimal sizePrice = new BigDecimal(size.getGia());

            // Xử lý topping
            BigDecimal toppingPrice = BigDecimal.ZERO;
            String toppingNames = "";
            if (itemRequest.getToppingIds() != null && !itemRequest.getToppingIds().isEmpty()) {
                for (Integer toppingId : itemRequest.getToppingIds()) {
                    Optional<Topping> toppingOpt = toppingRepository.findById(toppingId);
                    if (toppingOpt.isPresent()) {
                        toppingPrice = toppingPrice.add(new BigDecimal(toppingOpt.get().getGia()));
                        if (!toppingNames.isEmpty()) {
                            toppingNames += ", ";
                        }
                        toppingNames += toppingOpt.get().getTentopping();
                        log.info("Topping added: {} - {}", toppingId, toppingOpt.get().getTentopping());
                    }
                }
            }

            // Tính giá item = (giá sản phẩm + giá size + giá topping) * số lượng
            BigDecimal itemPrice = (new BigDecimal(product.getGia()).add(sizePrice).add(toppingPrice))
                    .multiply(new BigDecimal(itemRequest.getQuantity()));

            log.info("Item price calculated: {} (product: {}, size: {}, topping: {}, qty: {})", 
                itemPrice, product.getGia(), sizePrice, toppingPrice, itemRequest.getQuantity());

            // Tạo OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setProductName(product.getTensp());
            orderItem.setSizeId(itemRequest.getSizeId());
            orderItem.setSizeName(size.getTensize());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(new BigDecimal(product.getGia()));
            orderItem.setSizePrice(sizePrice);
            orderItem.setToppingPrice(toppingPrice);
            orderItem.setToppingNames(toppingNames);
            if (itemRequest.getToppingIds() != null) {
                orderItem.setToppingIds(itemRequest.getToppingIds().toString());
            }

            // Xử lý ice option
            if (itemRequest.getIceOptionId() != null) {
                Optional<IceOption> iceOpt = iceOptionRepository.findById(itemRequest.getIceOptionId());
                if (iceOpt.isPresent()) {
                    orderItem.setIceId(itemRequest.getIceOptionId());
                    orderItem.setIceOptionId(itemRequest.getIceOptionId());
                    orderItem.setIceOptionName(iceOpt.get().getTen_ice());
                    log.info("Ice option added: {}", iceOpt.get().getTen_ice());
                }
            }

            orderItem.setNotes(itemRequest.getNotes());

            return new ItemProcessResult(true, "Thành công", orderItem, itemPrice);

        } catch (Exception e) {
            log.error("Error processing order item: {}", e.getMessage(), e);
            return new ItemProcessResult(false, "Lỗi xử lý sản phẩm: " + e.getMessage(), null, BigDecimal.ZERO);
        }
    }

    /**
     * Inner class: Kết quả xử lý item
     */
    public static class ItemProcessResult {
        private boolean success;
        private String message;
        private OrderItem orderItem;
        private BigDecimal itemPrice;

        public ItemProcessResult(boolean success, String message, OrderItem orderItem, BigDecimal itemPrice) {
            this.success = success;
            this.message = message;
            this.orderItem = orderItem;
            this.itemPrice = itemPrice;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public OrderItem getOrderItem() {
            return orderItem;
        }

        public BigDecimal getItemPrice() {
            return itemPrice;
        }
    }
    
    /**
     * Result class for item details
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ItemDetailResult {
        private String productName;
        private String sizeName;
        private String toppingNames;
        private String iceOptionName;
        private BigDecimal productPrice;
        private BigDecimal sizePrice;
        private BigDecimal toppingPrice;
    }
}
