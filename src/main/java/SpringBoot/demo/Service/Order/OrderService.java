package SpringBoot.demo.Service.Order;

import SpringBoot.demo.DTO.*;
import SpringBoot.demo.Enum.DeliveryMethod;
import SpringBoot.demo.Enum.OrderStatus;
import SpringBoot.demo.Model.*;
import SpringBoot.demo.Repository.*;
import SpringBoot.demo.Service.Address.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service orchestrate tạo đơn hàng
 * Trách nhiệm: Điều phối các service khác (OrderItemService, OrderPricingService, AddressService)
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderStatusLogRepository orderStatusLogRepository;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderPricingService orderPricingService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Tạo đơn hàng mới
     */
    @Transactional
    public ApiResponse<OrderResponse> createOrder(CreateOrderRequest request, Long userId) {
        try {
            log.info("Creating order for user: {}", userId);

            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ApiResponse.error("Danh sách sản phẩm không được để trống");
            }

            Address address = validateAndGetAddress(request, userId);
            if (address == null && request.getDeliveryMethod() == DeliveryMethod.DELIVERY) {
                return ApiResponse.error("Địa chỉ không tồn tại");
            }

            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO;

            Order tempOrder = new Order();
            for (OrderItemRequest itemRequest : request.getItems()) {
                OrderItemService.ItemProcessResult result = orderItemService.processOrderItem(itemRequest, tempOrder);
                if (!result.isSuccess()) {
                    return ApiResponse.error(result.getMessage());
                }
                orderItems.add(result.getOrderItem());
                subtotal = subtotal.add(result.getItemPrice());
            }

            // Only apply tax for PICKUP, not for DELIVERY
            BigDecimal tax = BigDecimal.ZERO;
            if (request.getDeliveryMethod() == DeliveryMethod.PICKUP) {
                tax = orderPricingService.calculateTax(subtotal);
            }
            
            BigDecimal shippingFee = BigDecimal.ZERO;
            
            if (request.getDeliveryMethod() == DeliveryMethod.DELIVERY && address != null) {
                OrderPricingService.ShippingZoneResult shippingResult = 
                    orderPricingService.getShippingFee(address.getDistrict(), address.getCity());
                if (!shippingResult.isValid()) {
                    return ApiResponse.error(shippingResult.getMessage());
                }
                shippingFee = shippingResult.getShippingFee();
            }

            BigDecimal discount = BigDecimal.ZERO;
            String appliedVoucherCode = null;
            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                OrderPricingService.VoucherValidationResult voucherResult = 
                    orderPricingService.validateAndApplyVoucher(request.getVoucherCode(), subtotal);
                if (!voucherResult.isValid()) {
                    return ApiResponse.error(voucherResult.getMessage());
                }
                discount = voucherResult.getDiscount();
                appliedVoucherCode = request.getVoucherCode();
            }

            BigDecimal totalAmount = orderPricingService.calculateTotalAmount(subtotal, tax, shippingFee, discount);
            String orderCode = generateOrderCode();

            Order order = new Order();
            order.setUserId(userId);
            order.setOrderCode(orderCode);
            order.setTotalAmount(totalAmount);
            order.setTax(tax);
            order.setShippingFee(shippingFee);
            order.setDiscount(discount);
            order.setVoucherCode(appliedVoucherCode);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentMethod(request.getPaymentMethod());
            order.setDeliveryMethod(request.getDeliveryMethod());
            order.setAddressId(request.getAddressId());
            order.setNotes(request.getNotes());
            order.setCreatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);

            for (OrderItem item : orderItems) {
                item.setOrder(savedOrder);
            }
            orderItemRepository.saveAll(orderItems);

            OrderStatusLog statusLog = new OrderStatusLog(savedOrder, OrderStatus.PENDING, "Đơn hàng vừa được tạo");
            orderStatusLogRepository.save(statusLog);

            OrderResponse response = modelMapper.map(savedOrder, OrderResponse.class);
            response.setDiscount(savedOrder.getDiscount());
            response.setVoucherCode(savedOrder.getVoucherCode());
            return ApiResponse.success("Tạo đơn hàng thành công", response);

        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi tạo đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách đơn hàng của user
     */
    public ApiResponse<List<OrderResponse>> getOrdersByUser(Long userId) {
        try {
            List<Order> orders = orderRepository.findByUserId(userId);
            List<OrderResponse> responses = orders.stream()
                    .map(order -> modelMapper.map(order, OrderResponse.class))
                    .collect(Collectors.toList());
            return ApiResponse.success(responses);
        } catch (Exception e) {
            log.error("Error getting orders: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    public ApiResponse<OrderDetailResponse> getOrderDetail(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ApiResponse.error("Đơn hàng không tồn tại");
            }

            Order order = orderOpt.get();
            if (!order.getUserId().equals(userId)) {
                return ApiResponse.error("Bạn không có quyền xem đơn hàng này");
            }

            OrderDetailResponse response = modelMapper.map(order, OrderDetailResponse.class);
            response.setDiscount(order.getDiscount());
            response.setVoucherCode(order.getVoucherCode());

            // Set address nếu có
            if (order.getAddressId() != null) {
                addressService.getAddressById(order.getAddressId()).ifPresent(addr -> {
                    AddressResponse addressResponse = modelMapper.map(addr, AddressResponse.class);
                    response.setAddress(addressResponse);
                });
            }

            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            List<OrderItemResponse> itemResponses = items.stream()
                    .map(item -> {
                        OrderItemResponse itemResponse = modelMapper.map(item, OrderItemResponse.class);
                        BigDecimal subtotal = item.getPrice()
                                .add(item.getSizePrice() != null ? item.getSizePrice() : BigDecimal.ZERO)
                                .add(item.getToppingPrice() != null ? item.getToppingPrice() : BigDecimal.ZERO)
                                .multiply(new BigDecimal(item.getQuantity()));
                        itemResponse.setSubtotal(subtotal);
                        return itemResponse;
                    })
                    .collect(Collectors.toList());
            response.setItems(itemResponses);

            List<OrderStatusLog> statusLogs = orderStatusLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
            List<OrderStatusLogResponse> statusLogResponses = statusLogs.stream()
                    .map(log -> modelMapper.map(log, OrderStatusLogResponse.class))
                    .collect(Collectors.toList());
            response.setStatusLogs(statusLogResponses);

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Error getting order detail: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi lấy chi tiết đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Hủy đơn hàng
     */
    @Transactional
    public ApiResponse<String> cancelOrder(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ApiResponse.error("Đơn hàng không tồn tại");
            }

            Order order = orderOpt.get();
            if (!order.getUserId().equals(userId)) {
                return ApiResponse.error("Bạn không có quyền hủy đơn hàng này");
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                return ApiResponse.error("Chỉ có thể hủy đơn hàng ở trạng thái PENDING");
            }

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            OrderStatusLog statusLog = new OrderStatusLog(order, OrderStatus.CANCELLED, "Đơn hàng đã bị hủy");
            orderStatusLogRepository.save(statusLog);

            return ApiResponse.success("Hủy đơn hàng thành công");

        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi hủy đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử trạng thái đơn hàng
     */
    public ApiResponse<List<OrderStatusLogResponse>> getOrderStatusLog(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ApiResponse.error("Đơn hàng không tồn tại");
            }

            Order order = orderOpt.get();
            if (!order.getUserId().equals(userId)) {
                return ApiResponse.error("Bạn không có quyền xem lịch sử này");
            }

            List<OrderStatusLog> statusLogs = orderStatusLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
            List<OrderStatusLogResponse> responses = statusLogs.stream()
                    .map(log -> modelMapper.map(log, OrderStatusLogResponse.class))
                    .collect(Collectors.toList());

            return ApiResponse.success(responses);

        } catch (Exception e) {
            log.error("Error getting order status log: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi lấy lịch sử trạng thái: " + e.getMessage());
        }
    }

    /**
     * Helper: Validate và lấy address
     */
    private Address validateAndGetAddress(CreateOrderRequest request, Long userId) {
        if (request.getDeliveryMethod() == DeliveryMethod.DELIVERY) {
            if (request.getAddressId() == null) {
                return null;
            }

            if (!addressService.isAddressOwnedByUser(request.getAddressId(), userId)) {
                return null;
            }

            return addressService.getAddressById(request.getAddressId()).orElse(null);
        }
        return null;
    }

    /**
     * Helper: Tạo mã đơn hàng
     */
    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }
}
