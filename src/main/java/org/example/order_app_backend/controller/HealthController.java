package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.HealthValidationRequest;
import org.example.order_app_backend.dto.response.ServerStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "00. Health & Foundation", description = "Kiểm tra trạng thái hoạt động của hệ thống và kiểm thử cơ chế validation")
public class HealthController {

    @GetMapping
    @Operation(summary = "Kiểm tra tình trạng sống của Backend (Health Check)", description = "Trả về trạng thái hoạt động của server và thời gian hiện tại")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> statusData = Map.of(
                "status", "UP",
                "service", "order_app_backend",
                "framework", "Spring Boot 4.1.2",
                "javaVersion", System.getProperty("java.version"),
                "timestamp", Instant.now()
        );
        return ResponseEntity.ok(ApiResponse.success("Hệ thống Backend PRM393 Lab 3 đang hoạt động bình thường", statusData));
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping nhanh kiểm tra kết nối từ Flutter Client (VPS / Mobile)", description = "Trả về phản hồi tức thì để xác định địa chỉ máy chủ có thể tiếp cận được")
    public ResponseEntity<ApiResponse<ServerStatusResponse>> ping() {
        ServerStatusResponse response = ServerStatusResponse.builder()
                .status("UP")
                .message("Pong! Máy chủ Backend KTX PRM393 Lab 3 đang hoạt động sẵn sàng")
                .timestamp(Instant.now())
                .environment("Production-Ready")
                .version("Lab 3 - v1.0")
                .build();
        return ResponseEntity.ok(ApiResponse.success("Ping thành công", response));
    }

    @PostMapping("/test-validation")
    @Operation(summary = "Kiểm thử cơ chế Jakarta Validation & Error Response", description = "Endpoint phục vụ việc kiểm tra định dạng lỗi thống nhất từ GlobalExceptionHandler")
    public ResponseEntity<ApiResponse<HealthValidationRequest>> testValidation(@Valid @RequestBody HealthValidationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Dữ liệu hợp lệ", request));
    }
}
