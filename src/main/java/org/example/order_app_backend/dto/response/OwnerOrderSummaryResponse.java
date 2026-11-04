package org.example.order_app_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.order_app_backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerOrderSummaryResponse {
    private Long id;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String slotTime;
    private String pickupLocation;
    private BigDecimal finalAmount;
    private OrderStatus status;
    private String paymentMethod;
    private String note;
    private Instant createdAt;
    private List<OwnerOrderItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerOrderItemDto {
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal itemTotalAmount;
        private String selectedOptions;
    }
}