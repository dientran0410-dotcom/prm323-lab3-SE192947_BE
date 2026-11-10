package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.UpdateAvatarRequest;
import org.example.order_app_backend.dto.request.UpdateFcmTokenRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.service.FcmNotificationService;
import org.example.order_app_backend.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải lên ảnh đại diện cá nhân",
            description = "Tải lên tập tin ảnh (JPG, PNG, WEBP) lên Cloudinary và cập nhật hồ sơ người dùng hiện tại")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        UserResponse response = userService.uploadAvatar(file);
        return ResponseEntity.ok(ApiResponse.success("Tải lên ảnh đại diện thành công", response));
    }

    @GetMapping("/me/avatar")
    @Operation(summary = "Lấy đường dẫn ảnh đại diện cá nhân",
            description = "Truy xuất avatarUrl hiện tại của người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAvatar() {
        String avatarUrl = userService.getAvatarUrl();
        Map<String, String> data = new HashMap<>();
        data.put("avatarUrl", avatarUrl);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ảnh đại diện thành công", data));
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cập nhật trực tiếp đường dẫn ảnh đại diện",
            description = "Cập nhật avatarUrl từ một URL ảnh có sẵn")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @RequestBody UpdateAvatarRequest request) {
        UserResponse response = userService.updateAvatarUrl(request != null ? request.getAvatarUrl() : null);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh đại diện thành công", response));
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Xóa/đặt lại ảnh đại diện cá nhân",
            description = "Đặt avatarUrl của người dùng về null và xóa ảnh cũ trên Cloud Storage")
    public ResponseEntity<ApiResponse<UserResponse>> deleteAvatar() {
        UserResponse response = userService.deleteAvatar();
        return ResponseEntity.ok(ApiResponse.success("Xóa ảnh đại diện thành công", response));
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
