package SpringBoot.demo.DTO;

import SpringBoot.demo.Enum.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLogResponse {

    private Long id;

    private OrderStatus status;

    private String notes;

    private LocalDateTime createdAt;
}
