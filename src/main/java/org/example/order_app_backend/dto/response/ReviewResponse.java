package org.example.order_app_backend.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long orderId;
    private String orderCode;
    private Long storeId;
    private String storeName;
    private Long shipperId;
    private String shipperName;
    private Long userId;
    private String userDisplayName;
    private String userAvatarUrl;
    private Integer storeRating;
    private Integer shipperRating;
    private String comment;
    private String shipperComment;
    @Builder.Default
    private List<String> images = new ArrayList<>();
    private Instant createdAt;
}
