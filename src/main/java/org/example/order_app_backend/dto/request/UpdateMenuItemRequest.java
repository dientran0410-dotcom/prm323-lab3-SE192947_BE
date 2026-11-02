package org.example.order_app_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.order_app_backend.dto.response.CustomizationGroupDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật món ăn")
public class UpdateMenuItemRequest {

    @Schema(description = "Tên món ăn")
    private String name;

    @Schema(description = "Mô tả món ăn")
    private String description;

    @Schema(description = "Ảnh món ăn")
    private String imageUrl;

    @Positive(message = "Giá gốc phải lớn hơn 0")
    @Schema(description = "Giá gốc niêm yết")
    private BigDecimal basePrice;

    @Schema(description = "Danh mục: COM, BUN_PHO, NUOC, AN_VAT")
    private String category;

    @Schema(description = "Tình trạng còn món hay hết món")
    private Boolean isAvailable;

    @Schema(description = "Danh sách tùy biến (Size, Topping)")
    private List<CustomizationGroupDto> customizationOptions;
}

