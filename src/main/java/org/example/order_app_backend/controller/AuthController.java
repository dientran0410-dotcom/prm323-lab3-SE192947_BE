package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.LoginRequest;
import org.example.order_app_backend.dto.request.SyncUserRequest;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "01. Authentication & Sync", description = "Các API xác thực và đồng bộ người dùng từ Firebase Auth (Google Sign-In / OTP)")
public class AuthController {

    private final UserService userService;

    @PostMapping("/sync-user")
    @Operation(summary = "Đồng bộ / Đăng ký tài khoản sinh viên (Flow 0 - S0, S0_Reg)",
            description = "Tiếp nhận Firebase UID, email, tên và thông tin KTX sau khi đăng nhập thành công từ thiết bị di động, tự động tạo mới hoặc cập nhật hồ sơ.")
    public ResponseEntity<ApiResponse<UserResponse>> syncUser(@Valid @RequestBody SyncUserRequest request) {
        UserResponse response = userService.syncUser(request);
        return ResponseEntity.ok(ApiResponse.success("Đồng bộ tài khoản thành công", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập nhanh theo tài khoản 4 Role",
            description = "Xác thực tức thì bằng Email hoặc SĐT của 4 vai trò hệ thống")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }
}
