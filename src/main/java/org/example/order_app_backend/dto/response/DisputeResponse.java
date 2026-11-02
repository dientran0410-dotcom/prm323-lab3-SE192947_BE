package org.example.order_app_backend.dto.response;

import lombok.*;
import org.example.order_app_backend.entity.DisputeReason;
import org.example.order_app_backend.entity.DisputeStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeResponse {

    private Long id;
    private Long orderId;
    private String orderCode;
    private Long userId;
    private DisputeReason reason;
    private String description;
    private List<String> evidenceImages;
    private BigDecimal refundRequestedAmount;
    private BigDecimal refundApprovedAmount;
    private DisputeStatus status;
    private String resolutionNote;
    private Instant createdAt;
    private Instant updatedAt;
}

