package org.example.order_app_backend.mapper;

import org.example.order_app_backend.dto.response.ReviewResponse;
import org.example.order_app_backend.entity.ReviewEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(ReviewEntity entity, String storeName, String userDisplayName) {
        if (entity == null) {
            return null;
        }

        return ReviewResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .storeId(entity.getStoreId())
                .storeName(storeName)
                .userId(entity.getUserId())
                .userDisplayName(userDisplayName)
                .storeRating(entity.getStoreRating())
                .shipperRating(entity.getShipperRating())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

