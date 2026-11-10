package org.example.order_app_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_order_code", columnList = "order_code", unique = true),
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_store_id", columnList = "store_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_slot_time", columnList = "slot_time"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 32)
    private String orderCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "shipper_id")
    private Long shipperId;

    @Column(name = "delivery_batch_code", length = 50)
    private String deliveryBatchCode;

    @Column(name = "slot_time", nullable = false, length = 50)
    private String slotTime; // CA_TRUA_11H30, CA_CHIEU_18H00

    @Column(name = "pickup_location", nullable = false, length = 150)
    private String pickupLocation; // KTX Khu B – Trụ A1–A2 (Cổng B2)

    @Column(name = "subtotal_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotalAmount;

    @Column(name = "delivery_fee", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "group_discount_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal groupDiscountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "pickup_qr_code", nullable = false, length = 128)
    private String pickupQrCode;

    @Column(name = "pickup_pin", nullable = false, length = 8)
    private String pickupPin;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "shipper_arrived")
    @Builder.Default
    private Boolean shipperArrived = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

