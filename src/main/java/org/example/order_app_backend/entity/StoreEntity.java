package org.example.order_app_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stores", indexes = {
        @Index(name = "idx_stores_campus_zone", columnList = "campus_zone"),
        @Index(name = "idx_stores_name", columnList = "name"),
        @Index(name = "idx_stores_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "campus_zone", nullable = false, length = 50)
    private String campusZone; // KTX_KHU_A, KTX_KHU_B

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "rating_avg", precision = 3, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.valueOf(5.00);

    @Column(name = "rating_count", nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

