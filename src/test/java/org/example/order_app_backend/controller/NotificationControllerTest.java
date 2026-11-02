package org.example.order_app_backend.controller;

import org.example.order_app_backend.entity.NotificationEntity;
import org.example.order_app_backend.entity.NotificationType;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.example.order_app_backend.repository.NotificationRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;
    private NotificationEntity testNotification;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("student.noti.test@fpt.edu.vn").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email("student.noti.test@fpt.edu.vn")
                        .displayName("Sinh Viên Test Noti")
                        .phone("0955667788")
                        .role(UserRole.STUDENT)
                        .firebaseUid("noti_test_uid_001")
                        .build())
        );

        testNotification = notificationRepository.save(NotificationEntity.builder()
                .userId(testUser.getId())
                .title("Đơn hàng đã được xác nhận")
                .body("Quán ăn đang tiến hành làm món ăn của bạn.")
                .type(NotificationType.ORDER_STATUS)
                .isRead(false)
                .dataPayload("{\"orderId\": 501}")
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/notifications: Lấy danh sách thông báo thành công (200 OK)")
    void getMyNotifications_Success() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", not(empty())))
                .andExpect(jsonPath("$.data.content[0].title", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/{id}/read: Đánh dấu đã đọc thành công (200 OK)")
    void markAsRead_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/{id}/read", testNotification.getId())
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(testNotification.getId().intValue())))
                .andExpect(jsonPath("$.data.isRead", is(true)));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/read-all: Đánh dấu tất cả đã đọc thành công (200 OK)")
    void markAllAsRead_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("toàn bộ thông báo")));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count: Lấy số thông báo chưa đọc thành công (200 OK)")
    void getUnreadCount_Success() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.unreadCount", notNullValue()));
    }
}
