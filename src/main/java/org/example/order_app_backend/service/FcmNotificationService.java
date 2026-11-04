package org.example.order_app_backend.service;

import java.util.Map;

public interface FcmNotificationService {

    void updateDeviceToken(Long userId, String token, String deviceType);

    boolean sendPushNotification(Long targetUserId, String title, String body, Map<String, String> data);

    boolean sendOrderStatusPush(Long orderId, String status);
}
