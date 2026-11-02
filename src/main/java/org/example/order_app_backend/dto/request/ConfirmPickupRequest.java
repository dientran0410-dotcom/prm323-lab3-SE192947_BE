package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPickupRequest {

    @NotBlank(message = "Phương thức đối soát không được để trống (QR_SCAN hoặc PIN)")
    private String verificationMethod; // "QR_SCAN" or "PIN"

    @NotBlank(message = "Mã đối soát không được để trống")
    private String code;
}

