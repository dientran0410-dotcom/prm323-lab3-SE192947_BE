package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuItemAvailabilityRequest {

    @NotNull(message = "Trạng thái khả dụng không được để trống")
    private Boolean isAvailable;
}