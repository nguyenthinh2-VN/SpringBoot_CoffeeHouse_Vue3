package SpringBoot.demo.Enum;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    COD("Tiền mặt"),
    BANK_TRANSFER("Chuyển khoản"),
    VNPAY("VNPay"),
    MOMO("Momo");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
}
