package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.request.ClientCrashLogRequest;
import org.example.order_app_backend.service.SystemLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Tag(name = "00. System & Diagnostics", description = "Các API kiểm tra trạng thái và thu thập log sự cố từ Client")
public class SystemLogController {

    private final SystemLogService systemLogService;

    @PostMapping("/client-logs")
    @Operation(summary = "Tiếp nhận log sự cố từ thiết bị di động", description = "Lưu vết chẩn đoán Fatal/Non-Fatal crash từ client song song với Crashlytics")
    public ResponseEntity<ApiResponse<String>> receiveClientLog(@Valid @RequestBody ClientCrashLogRequest request) {
        systemLogService.recordClientCrashLog(request);
        return ResponseEntity.ok(ApiResponse.success("Đã tiếp nhận log sự cố thành công", "ACCEPTED"));
    }
}
