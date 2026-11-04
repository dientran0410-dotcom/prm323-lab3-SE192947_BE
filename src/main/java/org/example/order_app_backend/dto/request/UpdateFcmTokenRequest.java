package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFcmTokenRequest {

    @NotBlank(message = "FCM Token không được để trống")
    private String fcmToken;

    private String deviceType;

    private String timestamp;
}
