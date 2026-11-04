package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCrashLogRequest {

    private Long userId;

    private String userEmail;

    private String userRole;

    @NotBlank(message = "Loại sự cố không được để trống")
    private String crashType; // FATAL, NON_FATAL

    @NotBlank(message = "Thông điệp lỗi không được để trống")
    private String errorMessage;

    private String stackTrace;

    private String deviceModel;

    private String osVersion;

    private Map<String, Object> customAttributes;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
