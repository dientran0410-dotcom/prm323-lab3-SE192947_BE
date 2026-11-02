package org.example.order_app_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "menu_items", indexes = {
        @Index(name = "idx_menu_items_store_id", columnList = "store_id"),
        @Index(name = "idx_menu_items_category", columnList = "category"),
        @Index(name = "idx_menu_items_name", columnList = "name"),
        @Index(name = "idx_menu_items_is_available", columnList = "is_available")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "base_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal basePrice;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // COM, BUN_PHO, NUOC, AN_VAT

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "customization_options", columnDefinition = "TEXT")
    private String customizationOptions; // Lưu cấu trúc JSON Size, Topping

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

