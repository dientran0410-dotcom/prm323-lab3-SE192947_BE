package org.example.order_app_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết món ăn")
public class MenuItemResponse {

    @Schema(description = "ID món ăn", example = "101")
    private Long id;

    @Schema(description = "ID quán ăn sở hữu", example = "1")
    private Long storeId;

    @Schema(description = "Tên món ăn", example = "Cơm Sườn Nướng Chả Trứng")
    private String name;

    @Schema(description = "Mô tả món ăn", example = "Sườn nướng mật ong than hồng")
    private String description;

    @Schema(description = "Ảnh món ăn", example = "https://img.food.vn/suon.jpg")
    private String imageUrl;

    @Schema(description = "Giá gốc niêm yết (VND)", example = "35000.00")
    private BigDecimal basePrice;

    @Schema(description = "Danh mục món: COM, BUN_PHO, NUOC, AN_VAT", example = "COM")
    private String category;

    @Schema(description = "Tình trạng còn hàng", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Các nhóm tùy biến Size, Topping")
    private List<CustomizationGroupDto> customizationOptions;

    @Schema(description = "Thời gian tạo")
    private Instant createdAt;

    @Schema(description = "Thời gian cập nhật")
    private Instant updatedAt;
}

