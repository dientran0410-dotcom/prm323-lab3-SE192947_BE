package org.example.order_app_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id"),
        @Index(name = "idx_order_items_menu_item_id", columnList = "menu_item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "selected_options", columnDefinition = "TEXT")
    private String selectedOptions;

    @Column(name = "item_total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal itemTotalAmount;
}

