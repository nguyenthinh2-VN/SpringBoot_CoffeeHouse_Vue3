package SpringBoot.demo.Enum;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    PREPARING("Đang chuẩn bị"),
    READY_FOR_PICKUP("Sẵn sàng lấy"),
    COMPLETED("Hoàn thành"),
    SHIPPING("Đang giao"),
    OUT_FOR_DELIVERY("Đang giao"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy"),
    REFUNDED("Đã hoàn tiền");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
}
