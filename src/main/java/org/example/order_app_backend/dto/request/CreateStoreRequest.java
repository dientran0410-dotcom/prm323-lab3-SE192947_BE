package org.example.order_app_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới quán ăn")
public class CreateStoreRequest {

    @NotBlank(message = "Tên quán ăn không được để trống")
    @Schema(description = "Tên quán ăn", example = "Cơm Tấm Sườn Bì KTX B2")
    private String name;

    @Schema(description = "Mô tả quán ăn", example = "Cơm tấm nóng sốt, phục vụ tận trụ KTX")
    private String description;

    @NotBlank(message = "Địa chỉ quán không được để trống")
    @Schema(description = "Địa chỉ thực tế", example = "Cổng B2 KTX ĐHQG")
    private String address;

    @NotBlank(message = "Phân khu KTX không được để trống")
    @Schema(description = "Phân khu phục vụ: KTX_KHU_A hoặc KTX_KHU_B", example = "KTX_KHU_B")
    private String campusZone;

    @Schema(description = "Số điện thoại hotline quán", example = "0987123456")
    private String phoneNumber;

    @Schema(description = "Ảnh đại diện quán", example = "https://img.food.vn/comtam.jpg")
    private String avatarUrl;
}

