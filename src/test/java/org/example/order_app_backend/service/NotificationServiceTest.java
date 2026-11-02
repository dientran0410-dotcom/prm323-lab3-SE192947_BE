package org.example.order_app_backend.service;

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
import org.example.order_app_backend.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private NotificationMapper notificationMapper = new NotificationMapper();

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationEntity testNotification;

    @BeforeEach
    void setUp() {
        testNotification = NotificationEntity.builder()
                .id(101L)
                .userId(1L)
                .title("Đơn hàng đã được xác nhận")
                .body("Quán Cơm Tấm B2 đã tiếp nhận đơn KTX-20261102-0501 của bạn.")
                .type(NotificationType.ORDER_STATUS)
                .isRead(false)
                .dataPayload("{\"orderId\": 501}")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("getMyNotifications: Lấy danh sách thông báo của người dùng thành công")
    void getMyNotifications_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        Page<NotificationEntity> page = new PageImpl<>(List.of(testNotification));
        when(notificationRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        PageResponse<NotificationResponse> response = notificationService.getMyNotifications(PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(101L, response.getContent().get(0).getId());
        assertEquals("Đơn hàng đã được xác nhận", response.getContent().get(0).getTitle());
        assertFalse(response.getContent().get(0).getIsRead());
    }

    @Test
    @DisplayName("markAsRead: Đánh dấu đã đọc thành công khi đúng chủ sở hữu")
    void markAsRead_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(notificationRepository.findById(101L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(101L);

        assertNotNull(response);
        assertTrue(response.getIsRead());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("markAsRead: Không tìm thấy thông báo ném NOTIFICATION_NOT_FOUND")
    void markAsRead_NotFound_ThrowsException() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> notificationService.markAsRead(999L));
        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("markAsRead: Người dùng khác đánh dấu thông báo ném FORBIDDEN")
    void markAsRead_Forbidden_ThrowsException() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(2L);
        when(currentUserProvider.hasRole("ADMIN")).thenReturn(false);
        when(notificationRepository.findById(101L)).thenReturn(Optional.of(testNotification));

        AppException ex = assertThrows(AppException.class, () -> notificationService.markAsRead(101L));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAllAsRead: Đánh dấu toàn bộ thông báo đã đọc")
    void markAllAsRead_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        notificationService.markAllAsRead();

        verify(notificationRepository).markAllAsReadByUserId(1L);
    }

    @Test
    @DisplayName("getUnreadCount: Lấy số lượng thông báo chưa đọc")
    void getUnreadCount_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        UnreadNotificationCountResponse response = notificationService.getUnreadCount();

        assertNotNull(response);
        assertEquals(5L, response.getUnreadCount());
    }

    @Test
    @DisplayName("sendNotification: Tạo mới và lưu thông báo vào database")
    void sendNotification_Success() {
        notificationService.sendNotification(1L, "Giao dịch thành công", "Bạn đã thanh toán 85.000đ", NotificationType.PAYMENT_UPDATE, "{\"orderId\":501}");

        verify(notificationRepository).save(argThat(n ->
                n.getUserId().equals(1L) &&
                n.getTitle().equals("Giao dịch thành công") &&
                n.getType() == NotificationType.PAYMENT_UPDATE &&
                !n.getIsRead()
        ));
    }
}

