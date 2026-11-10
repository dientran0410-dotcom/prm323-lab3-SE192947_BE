package org.example.order_app_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Nhóm tùy biến món ăn (vd: Kích cỡ, Topping thêm)")
public class CustomizationGroupDto {

    @JsonAlias({"name", "group"})
    @Schema(description = "Tên nhóm tùy biến", example = "Topping thêm")
    private String group;

    @Schema(description = "Bắt buộc phải chọn hay không", example = "false")
    @Builder.Default
    private boolean required = false;

    @JsonAlias({"choices", "options"})
    @Schema(description = "Danh sách lựa chọn trong nhóm")
    private List<CustomizationOptionDto> options;
}


