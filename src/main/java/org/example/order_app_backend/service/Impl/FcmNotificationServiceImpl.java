package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.entity.OrderEntity;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.service.FcmNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationServiceImpl implements FcmNotificationService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void updateDeviceToken(Long userId, String token, String deviceType) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFcmToken(token);
            userRepository.save(user);
            log.info("FCM Service: Đã cập nhật Registration Token cho user {} (Thiết bị: {}, Token: {}...)",
                    user.getEmail(), deviceType, token.substring(0, Math.min(token.length(), 20)));
        });
    }

    @Override
    public boolean sendPushNotification(Long targetUserId, String title, String body, Map<String, String> data) {
        return userRepository.findById(targetUserId).map(user -> {
            String token = user.getFcmToken();
            if (token == null || token.isBlank()) {
                log.warn("FCM Service: Người dùng {} chưa đăng ký FCM token, bỏ qua đẩy thông báo", user.getEmail());
                return false;
            }
            log.info("FCM Service: [SIMULATION/SEND] Đang gửi Push Notification tới token: {}", token);
            log.info("   -> Title: {}", title);
            log.info("   -> Body: {}", body);
            log.info("   -> Data payload: {}", data);
            return true;
        }).orElse(false);
    }

    @Override
    public boolean sendOrderStatusPush(Long orderId, String status) {
        return orderRepository.findById(orderId).map(order -> {
            Long userId = order.getUserId();
            String title = "Cập nhật đơn hàng: " + order.getOrderCode();
            String body = switch (status.toUpperCase()) {
                case "PREPARING" -> "Quán ăn đang chuẩn bị món cho đơn hàng của bạn.";
                case "DELIVERING" -> "Shipper đang gom đơn và trên đường mang tới " + order.getPickupLocation();
                case "DELIVERED" -> "Đơn hàng đã tới sảnh KTX! Vui lòng dùng mã PIN " + order.getPickupPin() + " để nhận.";
                case "CANCELLED" -> "Đơn hàng của bạn đã bị hủy.";
                default -> "Đơn hàng của bạn đã chuyển sang trạng thái " + status;
            };

            return sendPushNotification(userId, title, body, Map.of(
                    "orderId", String.valueOf(orderId),
                    "orderCode", order.getOrderCode(),
                    "status", status
            ));
        }).orElse(false);
    }
}
