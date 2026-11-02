package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.response.NotificationResponse;
import org.example.order_app_backend.dto.response.UnreadNotificationCountResponse;
import org.example.order_app_backend.entity.NotificationType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PageResponse<NotificationResponse> getMyNotifications(Pageable pageable);
    NotificationResponse markAsRead(Long notificationId);
    void markAllAsRead();
    UnreadNotificationCountResponse getUnreadCount();
    void sendNotification(Long userId, String title, String body, NotificationType type, String dataPayload);
}
