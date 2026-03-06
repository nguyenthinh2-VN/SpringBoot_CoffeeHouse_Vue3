package SpringBoot.demo.Service.Order;

import SpringBoot.demo.DTO.OrderItemRequest;
import SpringBoot.demo.DTO.OrderPreviewRequest;
import SpringBoot.demo.DTO.OrderPreviewResponse;
import SpringBoot.demo.DTO.OrderItemResponse;
import SpringBoot.demo.Enum.DeliveryMethod;
import SpringBoot.demo.Model.Address;
import SpringBoot.demo.Service.Address.AddressService;
import SpringBoot.demo.Service.Order.Pricing.PricingStrategy;
import SpringBoot.demo.Service.Order.Pricing.UserPricingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service preview giá đơn hàng (chưa tạo đơn)
 * SRP: Chỉ tính giá preview, không tạo đơn
 * 
 * Dùng để FE hiển thị giá trước khi user đặt hàng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPreviewService {
    
    private final UserPricingStrategy userPricingStrategy;
    private final OrderItemService orderItemService;
    private final OrderPricingService orderPricingService;
    private final AddressService addressService;
    private final ModelMapper modelMapper;
    
    /**
     * Preview giá đơn hàng
     */
    public OrderPreviewResponse previewOrder(OrderPreviewRequest request) {
        try {
            log.info("Previewing order: items={}, addressId={}, voucherCode={}", 
                     request.getItems().size(), request.getAddressId(), request.getVoucherCode());
            
            // 1. Tính subtotal
            BigDecimal subtotal = calculateSubtotal(request.getItems());
            log.info("Subtotal: {}", subtotal);
            
            // 2. Tính tax (chỉ áp dụng cho PICKUP, không áp dụng cho DELIVERY)
            BigDecimal tax = BigDecimal.ZERO;
            if (request.getDeliveryMethod() == DeliveryMethod.PICKUP) {
                tax = userPricingStrategy.calculateTax(subtotal);
            }
            log.info("Tax: {} (DeliveryMethod: {})", tax, request.getDeliveryMethod());
            
            // 3. Tính shipping fee
            BigDecimal shippingFee = BigDecimal.ZERO;
            if (request.getDeliveryMethod() == DeliveryMethod.DELIVERY && request.getAddressId() != null) {
                // Lấy địa chỉ từ DB
                Optional<Address> addressOpt = addressService.getAddressById(request.getAddressId());
                if (addressOpt.isPresent()) {
                    Address address = addressOpt.get();
                    // Tính phí ship dựa trên district và city
                    OrderPricingService.ShippingZoneResult shippingResult = 
                        orderPricingService.getShippingFee(address.getDistrict(), address.getCity());
                    
                    if (shippingResult.isValid()) {
                        shippingFee = shippingResult.getShippingFee();
                        log.info("Shipping fee: {} VND (DELIVERY to {}, {})", shippingFee, address.getDistrict(), address.getCity());
                    } else {
                        log.warn("Shipping zone not supported: {} - {}", address.getDistrict(), address.getCity());
                        shippingFee = BigDecimal.ZERO;
                    }
                } else {
                    log.warn("Address not found: id={}", request.getAddressId());
                    shippingFee = BigDecimal.ZERO;
                }
            } else {
                log.info("Shipping fee: 0 (PICKUP)");
            }
            
            // 4. Tính discount từ voucher
            BigDecimal discount = BigDecimal.ZERO;
            String appliedVoucherCode = null;
            
            // Chỉ validate voucher nếu có nhập
            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                try {
                    discount = userPricingStrategy.calculateDiscount(request.getVoucherCode(), subtotal);
                    appliedVoucherCode = request.getVoucherCode();
                    log.info("Discount: {} (voucher: {})", discount, appliedVoucherCode);
                } catch (Exception e) {
                    log.warn("Voucher validation failed: {}", e.getMessage());
                    // Không throw, chỉ log warning
                }
            } else {
                log.info("No voucher code provided");
            }
            
            // 5. Tính total
            BigDecimal totalAmount = userPricingStrategy.calculateTotal(subtotal, tax, shippingFee, discount);
            log.info("Total: {}", totalAmount);
            
            // 6. Map items with details
            List<OrderItemResponse> itemResponses = request.getItems().stream()
                    .map(item -> {
                        try {
                            // Get item details from DB
                            var itemDetail = orderItemService.getItemDetails(item);
                            
                            // Calculate item subtotal = (price + sizePrice + toppingPrice) × quantity
                            BigDecimal itemSubtotal = itemDetail.getProductPrice()
                                    .add(itemDetail.getSizePrice() != null ? itemDetail.getSizePrice() : BigDecimal.ZERO)
                                    .add(itemDetail.getToppingPrice() != null ? itemDetail.getToppingPrice() : BigDecimal.ZERO)
                                    .multiply(new BigDecimal(item.getQuantity()));
                            
                            OrderItemResponse response = new OrderItemResponse();
                            response.setProductId(item.getProductId());
                            response.setProductName(itemDetail.getProductName());
                            response.setSizeId(item.getSizeId());
                            response.setSizeName(itemDetail.getSizeName());
                            response.setQuantity(item.getQuantity());
                            response.setPrice(itemDetail.getProductPrice());
                            response.setSizePrice(itemDetail.getSizePrice());
                            response.setToppingPrice(itemDetail.getToppingPrice());
                            response.setSubtotal(itemSubtotal);
                            
                            // Convert List<Integer> to String
                            if (item.getToppingIds() != null && !item.getToppingIds().isEmpty()) {
                                response.setToppingIds(item.getToppingIds().toString());
                                response.setToppingNames(itemDetail.getToppingNames());
                            }
                            
                            response.setIceOptionId(item.getIceOptionId());
                            response.setIceOptionName(itemDetail.getIceOptionName());
                            response.setNotes(item.getNotes());
                            
                            return response;
                        } catch (Exception e) {
                            log.error("Error mapping item details: {}", e.getMessage());
                            throw new RuntimeException("Lỗi lấy thông tin sản phẩm: " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());
            
            // 7. Build response
            OrderPreviewResponse response = new OrderPreviewResponse();
            response.setSubtotal(subtotal);
            response.setTax(tax);
            response.setShippingFee(shippingFee);
            response.setDiscount(discount);
            response.setVoucherCode(appliedVoucherCode);
            response.setTotalAmount(totalAmount);
            response.setItems(itemResponses);
            response.setMessage("Giá có thể thay đổi khi đặt hàng. Vui lòng xác nhận trước khi thanh toán.");
            
            log.info("Order preview completed successfully");
            return response;
            
        } catch (Exception e) {
            log.error("Error previewing order: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi tính giá: " + e.getMessage());
        }
    }
    
    /**
     * Tính subtotal từ items (lấy giá từ DB)
     */
    private BigDecimal calculateSubtotal(List<OrderItemRequest> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (OrderItemRequest item : items) {
            try {
                // Dùng OrderItemService để tính giá item
                BigDecimal itemPrice = orderItemService.calculateItemPrice(item);
                BigDecimal totalItemPrice = itemPrice.multiply(new BigDecimal(item.getQuantity()));
                subtotal = subtotal.add(totalItemPrice);
                log.info("Item price calculated: {} × {} = {} (product: {})", 
                         itemPrice, item.getQuantity(), totalItemPrice, item.getProductId());
            } catch (Exception e) {
                log.error("Error calculating item price: {}", e.getMessage());
                throw new RuntimeException("Lỗi tính giá sản phẩm: " + e.getMessage());
            }
        }
        
        return subtotal;
    }
}
