package org.example.order_app_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nhóm tùy biến món ăn (vd: Kích cỡ, Topping thêm)")
public class CustomizationGroupDto {

    @Schema(description = "Tên nhóm tùy biến", example = "Topping thêm")
    private String group;

    @Schema(description = "Bắt buộc phải chọn hay không", example = "false")
    @Builder.Default
    private boolean required = false;

    @Schema(description = "Danh sách lựa chọn trong nhóm")
    private List<CustomizationOptionDto> options;
}

