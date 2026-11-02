package org.example.order_app_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Yêu cầu tạo món ăn mới cho quán")
public class CreateMenuItemRequest {

    @NotBlank(message = "Tên món ăn không được để trống")
    @Schema(description = "Tên món", example = "Cơm Sườn Nướng Chả Trứng")
    private String name;

    @Schema(description = "Mô tả món ăn", example = "Sườn nướng mật ong than hồng")
    private String description;

    @Schema(description = "Ảnh món ăn", example = "https://img.food.vn/suon.jpg")
    private String imageUrl;

    @NotNull(message = "Giá gốc không được để trống")
    @Positive(message = "Giá gốc phải lớn hơn 0")
    @Schema(description = "Giá gốc niêm yết (VND)", example = "35000.00")
    private BigDecimal basePrice;

    @NotBlank(message = "Danh mục không được để trống")
    @Schema(description = "Danh mục món: COM, BUN_PHO, NUOC, AN_VAT", example = "COM")
    private String category;

    @Schema(description = "Danh sách tùy biến (Size, Topping)")
    private List<CustomizationGroupDto> customizationOptions;
}

