package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.HealthValidationRequest;
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

    @PostMapping("/test-validation")
    @Operation(summary = "Kiểm thử cơ chế Jakarta Validation & Error Response", description = "Endpoint phục vụ việc kiểm tra định dạng lỗi thống nhất từ GlobalExceptionHandler")
    public ResponseEntity<ApiResponse<HealthValidationRequest>> testValidation(@Valid @RequestBody HealthValidationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Dữ liệu hợp lệ", request));
    }
}
