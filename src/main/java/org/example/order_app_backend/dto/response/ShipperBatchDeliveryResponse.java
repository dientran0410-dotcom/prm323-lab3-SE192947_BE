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
public class ShipperBatchDeliveryResponse {
    private String slotTime;
    private String campusZone;
    private Integer totalOrders;
    private BigDecimal totalDeliveryEarnings;
    private List<ShipperOrderItemDto> orders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipperOrderItemDto {
        private Long orderId;
        private String orderCode;
        private String storeName;
        private String storeAddress;
        private String pickupLocation;
        private String customerName;
        private String customerPhone;
        private String paymentMethod;
        private BigDecimal finalAmount;
        private OrderStatus status;
        private String pickupPin;
        private String pickupQrCode;
        private Instant createdAt;
    }
}