package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectedItemOptionDto {

    @NotBlank(message = "Tên nhóm tùy biến không được để trống")
    private String group;

    @NotBlank(message = "Lựa chọn tùy biến không được để trống")
    private String choice;

    @Builder.Default
    private BigDecimal extraPrice = BigDecimal.ZERO;
}

