package org.example.order_app_backend.mapper;

import org.example.order_app_backend.dto.request.CreateStoreRequest;
import org.example.order_app_backend.dto.request.UpdateStoreRequest;
import org.example.order_app_backend.dto.response.StoreResponse;
import org.example.order_app_backend.entity.StoreEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StoreMapper {

    public StoreResponse toResponse(StoreEntity entity) {
        if (entity == null) {
            return null;
        }

        return StoreResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .address(entity.getAddress())
                .campusZone(entity.getCampusZone())
                .phoneNumber(entity.getPhoneNumber())
                .avatarUrl(entity.getAvatarUrl())
                .isActive(entity.getIsActive())
                .ratingAvg(entity.getRatingAvg())
                .ratingCount(entity.getRatingCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public StoreEntity toEntity(CreateStoreRequest request) {
        if (request == null) {
            return null;
        }

        return StoreEntity.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .address(request.getAddress().trim())
                .campusZone(request.getCampusZone().trim())
                .phoneNumber(request.getPhoneNumber())
                .avatarUrl(request.getAvatarUrl())
                .isActive(true)
                .ratingAvg(BigDecimal.valueOf(5.00))
                .ratingCount(0)
                .build();
    }

    public void updateEntityFromRequest(StoreEntity entity, UpdateStoreRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            entity.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            entity.setAddress(request.getAddress().trim());
        }
        if (request.getCampusZone() != null && !request.getCampusZone().isBlank()) {
            entity.setCampusZone(request.getCampusZone().trim());
        }
        if (request.getPhoneNumber() != null) {
            entity.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAvatarUrl() != null) {
            entity.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
    }
}

