package org.example.order_app_backend.mapper;

import org.example.order_app_backend.dto.response.NotificationResponse;
import org.example.order_app_backend.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .type(entity.getType())
                .isRead(entity.getIsRead())
                .dataPayload(entity.getDataPayload())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
