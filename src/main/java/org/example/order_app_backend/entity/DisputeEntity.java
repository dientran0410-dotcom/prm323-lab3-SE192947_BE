package org.example.order_app_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "disputes", indexes = {
        @Index(name = "idx_disputes_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_disputes_user_id", columnList = "user_id"),
        @Index(name = "idx_disputes_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private DisputeReason reason;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_images", nullable = false, columnDefinition = "TEXT")
    private String evidenceImages;

    @Column(name = "refund_requested_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal refundRequestedAmount;

    @Column(name = "refund_approved_amount", precision = 12, scale = 2)
    private BigDecimal refundApprovedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.PENDING;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

