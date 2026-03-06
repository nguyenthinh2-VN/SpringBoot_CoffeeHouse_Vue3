package SpringBoot.demo.Enum;

/**
 * Payment status enum
 */
public enum PaymentStatus {
    UNPAID,     // Cũ - compatibility
    PENDING,    // Chờ thanh toán
    PAID,       // Đã thanh toán
    FAILED,     // Thanh toán thất bại
    CANCELLED   // Hủy
}
