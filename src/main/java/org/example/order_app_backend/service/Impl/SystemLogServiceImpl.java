package org.example.order_app_backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.request.ClientCrashLogRequest;
import org.example.order_app_backend.service.SystemLogService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SystemLogServiceImpl implements SystemLogService {

    @Override
    public void recordClientCrashLog(ClientCrashLogRequest request) {
        log.warn("=== TIẾP NHẬN BÁO CÁO SỰ CỐ TỪ CLIENT (CRASH LOG) ===");
        log.warn("Thời gian: {}", request.getTimestamp());
        log.warn("Người dùng: {} (ID: {}) - Vai trò: {}", request.getUserEmail(), request.getUserId(), request.getUserRole());
        log.warn("Loại sự cố: {} | Thiết bị: {} (OS: {})", request.getCrashType(), request.getDeviceModel(), request.getOsVersion());
        log.warn("Thông điệp lỗi: {}", request.getErrorMessage());
        if (request.getStackTrace() != null) {
            log.warn("StackTrace: {}", request.getStackTrace());
        }
        if (request.getCustomAttributes() != null && !request.getCustomAttributes().isEmpty()) {
            log.warn("Thuộc tính bổ sung: {}", request.getCustomAttributes());
        }
        log.warn("=====================================================");
    }
}

