package SpringBoot.demo.Enum;

import lombok.Getter;

@Getter
public enum DeliveryMethod {
    DELIVERY("Giao tận nơi"),
    PICKUP("Lấy tại quán");

    private final String displayName;

    DeliveryMethod(String displayName) {
        this.displayName = displayName;
    }
}
