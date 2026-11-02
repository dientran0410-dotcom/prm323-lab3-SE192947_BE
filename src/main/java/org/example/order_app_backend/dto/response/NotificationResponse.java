package org.example.order_app_backend.dto.response;

import lombok.*;
import org.example.order_app_backend.entity.NotificationType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String title;
    private String body;
    private NotificationType type;
    private Boolean isRead;
    private String dataPayload;
    private Instant createdAt;
}

