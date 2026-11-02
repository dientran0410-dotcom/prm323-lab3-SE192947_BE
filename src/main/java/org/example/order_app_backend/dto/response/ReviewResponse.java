package org.example.order_app_backend.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long orderId;
    private Long storeId;
    private String storeName;
    private Long userId;
    private String userDisplayName;
    private Integer storeRating;
    private Integer shipperRating;
    private String comment;
    private Instant createdAt;
}

