package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.order_app_backend.entity.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus status;
}

