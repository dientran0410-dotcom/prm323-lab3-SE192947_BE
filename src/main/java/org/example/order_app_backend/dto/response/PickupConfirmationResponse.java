package org.example.order_app_backend.dto.response;

import lombok.*;
import org.example.order_app_backend.entity.OrderStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupConfirmationResponse {

    private Long orderId;
    private OrderStatus status;
    private Instant deliveredAt;
}

