package SpringBoot.demo.Enum;

/**
 * Loại đơn hàng
 * SRP: Chỉ định nghĩa các loại đơn
 */
public enum OrderType {
    USER,   // Đơn hàng từ khách online
    STAFF   // Đơn hàng từ staff/admin tại quán
}
