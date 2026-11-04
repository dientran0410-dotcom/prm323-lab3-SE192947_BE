package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.UpdateFcmTokenRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.service.FcmNotificationService;
import org.example.order_app_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "02. User Profile", description = "Quản lý hồ sơ cá nhân sinh viên, thông tin phòng KTX")
public class UserController {

    private final UserService userService;
    private final FcmNotificationService fcmNotificationService;

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin hồ sơ của người dùng hiện tại (Flow 0 - S13)",
            description = "Truy xuất thông tin sinh viên đang đăng nhập thông qua abstraction CurrentUserProvider.")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin hồ sơ thành công", response));
    }

    @PatchMapping("/me")
    @Operation(summary = "Cập nhật hồ sơ cá nhân (S13 Profile)",
            description = "Cho phép cập nhật tên, số điện thoại, tòa KTX, số phòng và ảnh đại diện.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", response));
    }

    @PostMapping("/me/fcm-token")
    @Operation(summary = "Đăng ký hoặc cập nhật FCM Registration Token cho thiết bị",
            description = "Lưu token thông báo đẩy của thiết bị di động/Web phục vụ nhận thông báo đơn hàng")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateFcmToken(
            @Valid @RequestBody UpdateFcmTokenRequest request) {
        Long currentUserId = userService.getCurrentUserProfile().getId();
        fcmNotificationService.updateDeviceToken(currentUserId, request.getFcmToken(), request.getDeviceType());
        return ResponseEntity.ok(ApiResponse.success("Đã đồng bộ FCM Token thành công", Map.of(
                "userId", currentUserId,
                "fcmToken", request.getFcmToken()
        )));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem thông tin người dùng theo ID",
            description = "Truy xuất hồ sơ sinh viên hoặc chủ quán theo mã định danh nội bộ.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", response));
    }
}
