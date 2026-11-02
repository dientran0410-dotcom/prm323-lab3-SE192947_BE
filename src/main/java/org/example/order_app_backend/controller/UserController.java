package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "02. User Profile", description = "Quản lý hồ sơ cá nhân sinh viên, thông tin phòng KTX")
public class UserController {

    private final UserService userService;

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

    @GetMapping("/{id}")
    @Operation(summary = "Xem thông tin người dùng theo ID",
            description = "Truy xuất hồ sơ sinh viên hoặc chủ quán theo mã định danh nội bộ.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", response));
    }
}

