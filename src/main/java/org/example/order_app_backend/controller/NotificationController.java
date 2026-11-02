package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.response.NotificationResponse;
import org.example.order_app_backend.dto.response.UnreadNotificationCountResponse;
import org.example.order_app_backend.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Các API trung tâm thông báo đơn hàng và cập nhật trạng thái đã đọc")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo của tôi (API-019)", description = "Lấy danh sách lịch sử thông báo của sinh viên đang đăng nhập kèm phân trang")
    public ApiResponse<PageResponse<NotificationResponse>> getMyNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: Lấy danh sách thông báo người dùng");
        PageResponse<NotificationResponse> response = notificationService.getMyNotifications(pageable);
        return ApiResponse.<PageResponse<NotificationResponse>>builder()
                .success(true)
                .message("Lấy danh sách thông báo thành công")
                .data(response)
                .build();
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Đánh dấu thông báo đã đọc (API-020)", description = "Cập nhật trạng thái đã đọc cho một thông báo cụ thể")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("API: Đánh dấu đã đọc cho thông báo ID: {}", id);
        NotificationResponse response = notificationService.markAsRead(id);
        return ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Đã đánh dấu thông báo là đã đọc")
                .data(response)
                .build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc (API-021)", description = "Cập nhật tất cả thông báo chưa đọc của sinh viên sang trạng thái đã đọc")
    public ApiResponse<Void> markAllAsRead() {
        log.info("API: Đánh dấu tất cả thông báo đã đọc");
        notificationService.markAllAsRead();
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Đã đánh dấu toàn bộ thông báo là đã đọc")
                .build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Đếm số thông báo chưa đọc (API-022)", description = "Lấy tổng số lượng thông báo chưa đọc để hiển thị badge trên icon chuông màn hình Flutter")
    public ApiResponse<UnreadNotificationCountResponse> getUnreadCount() {
        log.info("API: Lấy số lượng thông báo chưa đọc");
        UnreadNotificationCountResponse response = notificationService.getUnreadCount();
        return ApiResponse.<UnreadNotificationCountResponse>builder()
                .success(true)
                .message("Lấy số thông báo chưa đọc thành công")
                .data(response)
                .build();
    }
}

