package org.example.order_app_backend.dto.response;

import lombok.*;
import org.example.order_app_backend.entity.OrderStatus;
import org.example.order_app_backend.entity.PaymentMethod;
import org.example.order_app_backend.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderCode;
    private Long userId;
    private Long storeId;
    private String storeName;
    private String slotTime;
    private String pickupLocation;
    private BigDecimal subtotalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal groupDiscountAmount;
    private BigDecimal finalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String pickupQrCode;
    private String pickupPin;
    private String note;
    private Boolean shipperArrived;
    private List<OrderItemResponse> items;
    private Instant deliveredAt;
    private Instant createdAt;
    private Instant updatedAt;
}

