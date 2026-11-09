package org.example.order_app_backend.mapper;

import org.example.order_app_backend.dto.request.SyncUserRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserResponse.builder()
                .id(entity.getId())
                .firebaseUid(entity.getFirebaseUid())
                .email(entity.getEmail())
                .displayName(entity.getDisplayName())
                .phone(entity.getPhone())
                .role(entity.getRole() != null ? entity.getRole().name() : UserRole.STUDENT.name())
                .avatarUrl(entity.getAvatarUrl())
                .ktxBuilding(entity.getKtxBuilding())
                .ktxRoom(entity.getKtxRoom())
                .ratingAvg(entity.getRatingAvg())
                .ratingCount(entity.getRatingCount())
                .isBanned(entity.getIsBanned() != null ? entity.getIsBanned() : false)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserEntity toEntity(SyncUserRequest request) {
        if (request == null) {
            return null;
        }

        return UserEntity.builder()
                .firebaseUid(request.getFirebaseUid())
                .email(request.getEmail().toLowerCase().trim())
                .displayName(request.getDisplayName().trim())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .ktxBuilding(request.getKtxBuilding())
                .ktxRoom(request.getKtxRoom())
                .role(UserRole.STUDENT)
                .build();
    }

    public void updateEntityFromSync(UserEntity entity, SyncUserRequest request) {
        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            entity.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            entity.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            entity.setPhone(request.getPhone().trim());
        }
        if (request.getKtxBuilding() != null && !request.getKtxBuilding().isBlank()) {
            entity.setKtxBuilding(request.getKtxBuilding().trim());
        }
        if (request.getKtxRoom() != null && !request.getKtxRoom().isBlank()) {
            entity.setKtxRoom(request.getKtxRoom().trim());
        }
        if (entity.getFirebaseUid() == null || entity.getFirebaseUid().isBlank()) {
            entity.setFirebaseUid(request.getFirebaseUid());
        }
    }

    public void updateEntityFromRequest(UserEntity entity, UpdateUserProfileRequest request) {
        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            entity.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            entity.setPhone(request.getPhone().trim());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            entity.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getKtxBuilding() != null && !request.getKtxBuilding().isBlank()) {
            entity.setKtxBuilding(request.getKtxBuilding().trim());
        }
        if (request.getKtxRoom() != null && !request.getKtxRoom().isBlank()) {
            entity.setKtxRoom(request.getKtxRoom().trim());
        }
    }
}
