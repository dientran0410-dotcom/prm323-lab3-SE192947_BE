package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.order_app_backend.entity.DisputeStatus;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolveDisputeRequest {

    @NotNull(message = "Trạng thái xử lý khiếu nại không được để trống (APPROVED hoặc REJECTED)")
    private DisputeStatus status;

    private BigDecimal refundApprovedAmount;

    private String resolutionNote;
}

