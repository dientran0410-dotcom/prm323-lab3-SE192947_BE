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
    private final org.example.order_app_backend.service.OtpService otpService;

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

    @PostMapping("/forgot-password/send-otp")
    @Operation(summary = "Gửi mã xác thực OTP khôi phục mật khẩu",
            description = "Sinh mã OTP ngẫu nhiên 6 chữ số, in trực tiếp ra Terminal Server Console và trả về cho client.")
    public ResponseEntity<ApiResponse<org.example.order_app_backend.dto.response.OtpResponse>> sendForgotPasswordOtp(
            @Valid @RequestBody org.example.order_app_backend.dto.request.SendOtpRequest request) {
        String otp = otpService.generateAndSendOtp(request.getPhoneNumber());
        org.example.order_app_backend.dto.response.OtpResponse response = org.example.order_app_backend.dto.response.OtpResponse.builder()
                .phoneNumber(request.getPhoneNumber())
                .otp(otp)
                .expiresInSeconds(300)
                .message("Mã OTP đã được gửi và hiển thị trên màn hình Terminal của máy chủ.")
                .build();
        return ResponseEntity.ok(ApiResponse.success("Gửi mã OTP thành công", response));
    }

    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Xác nhận OTP và đổi mật khẩu mới",
            description = "Kiểm tra mã OTP nhập từ Terminal Console và lưu mật khẩu mới vào cơ sở dữ liệu.")
    public ResponseEntity<ApiResponse<UserResponse>> resetPassword(
            @Valid @RequestBody org.example.order_app_backend.dto.request.ResetPasswordRequest request) {
        UserResponse response = userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công", response));
    }

    @org.springframework.web.bind.annotation.GetMapping("/forgot-password/latest-otp")
    @Operation(summary = "Tra cứu mã OTP đang hoạt động (Dành cho Swagger Tester)",
            description = "Xem lại mã OTP hiện tại chưa hết hạn của một số điện thoại.")
    public ResponseEntity<ApiResponse<org.example.order_app_backend.dto.response.OtpResponse>> getLatestOtp(
            @org.springframework.web.bind.annotation.RequestParam String phoneNumber) {
        String otp = otpService.getLatestOtp(phoneNumber);
        if (otp == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<org.example.order_app_backend.dto.response.OtpResponse>builder()
                    .success(false)
                    .message("Không có mã OTP nào đang hoạt động cho số này hoặc đã hết hạn.")
                    .build());
        }
        org.example.order_app_backend.dto.response.OtpResponse response = org.example.order_app_backend.dto.response.OtpResponse.builder()
                .phoneNumber(phoneNumber)
                .otp(otp)
                .expiresInSeconds(300)
                .message("Mã OTP hiện tại đang còn hiệu lực.")
                .build();
        return ResponseEntity.ok(ApiResponse.success("Tra cứu OTP thành công", response));
    }
}
