package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.response.NotificationResponse;
import org.example.order_app_backend.dto.response.UnreadNotificationCountResponse;
import org.example.order_app_backend.entity.NotificationEntity;
import org.example.order_app_backend.entity.NotificationType;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.NotificationMapper;
import org.example.order_app_backend.repository.NotificationRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Pageable pageable) {
        Long currentUserId = getCurrentUserIdOrThrow();
        log.info("Lấy danh sách thông báo của người dùng ID: {}", currentUserId);

        Page<NotificationEntity> page = notificationRepository.findByUserId(currentUserId, pageable);
        List<NotificationResponse> content = page.getContent().stream()
                .map(notificationMapper::toResponse)
                .toList();

        return PageResponse.<NotificationResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Long currentUserId = getCurrentUserIdOrThrow();
        log.info("Đánh dấu đã đọc thông báo ID: {} bởi người dùng ID: {}", notificationId, currentUserId);

        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(currentUserId) && !currentUserProvider.hasRole("ADMIN")) {
            log.warn("Truy cập trái phép: Người dùng {} cố tình đánh dấu thông báo của người dùng {}", currentUserId, notification.getUserId());
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        notification.setIsRead(true);
        NotificationEntity updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long currentUserId = getCurrentUserIdOrThrow();
        log.info("Đánh dấu tất cả thông báo là đã đọc cho người dùng ID: {}", currentUserId);
        notificationRepository.markAllAsReadByUserId(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount() {
        Long currentUserId = getCurrentUserIdOrThrow();
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(currentUserId);
        return UnreadNotificationCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }

    private Long getCurrentUserIdOrThrow() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return currentUserId;
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, String title, String body, NotificationType type, String dataPayload) {
        log.info("Gửi thông báo tới người dùng ID: {}, Tiêu đề: {}, Loại: {}", userId, title, type);
        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .type(type)
                .isRead(false)
                .dataPayload(dataPayload)
                .build();

        notificationRepository.save(notification);
        // Trong tương lai khi tích hợp Firebase Cloud Messaging (FCM), gửi push message tại đây.
    }
}
