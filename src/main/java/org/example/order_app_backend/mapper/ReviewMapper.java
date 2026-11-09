package org.example.order_app_backend.mapper;

import org.example.order_app_backend.dto.response.ReviewResponse;
import org.example.order_app_backend.entity.ReviewEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(ReviewEntity entity, String storeName, String userDisplayName) {
        return toResponse(entity, storeName, userDisplayName, null, null, null);
    }

    public ReviewResponse toResponse(
            ReviewEntity entity,
            String storeName,
            String userDisplayName,
            String userAvatarUrl,
            String orderCode,
            String shipperName) {
        if (entity == null) {
            return null;
        }

        List<String> imageList = new ArrayList<>();
        if (entity.getImages() != null && !entity.getImages().isBlank()) {
            imageList = Arrays.stream(entity.getImages().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        return ReviewResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .orderCode(orderCode != null ? orderCode : "#KTX-" + entity.getOrderId())
                .storeId(entity.getStoreId())
                .storeName(storeName)
                .shipperId(entity.getShipperId())
                .shipperName(shipperName != null ? shipperName : "Tài xế Gom KTX")
                .userId(entity.getUserId())
                .userDisplayName(userDisplayName)
                .userAvatarUrl(userAvatarUrl)
                .storeRating(entity.getStoreRating())
                .shipperRating(entity.getShipperRating())
                .comment(entity.getComment())
                .shipperComment(entity.getShipperComment())
                .images(imageList)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
