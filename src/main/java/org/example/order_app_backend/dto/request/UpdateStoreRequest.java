package org.example.order_app_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin quán ăn")
public class UpdateStoreRequest {

    @Schema(description = "Tên quán ăn", example = "Cơm Tấm Sườn Bì KTX B2 (Mới)")
    private String name;

    @Schema(description = "Mô tả quán", example = "Phục vụ ca trưa và tối")
    private String description;

    @Schema(description = "Địa chỉ thực tế")
    private String address;

    @Schema(description = "Phân khu KTX: KTX_KHU_A, KTX_KHU_B")
    private String campusZone;

    @Schema(description = "Số điện thoại")
    private String phoneNumber;

    @Schema(description = "Ảnh đại diện")
    private String avatarUrl;

    @Schema(description = "Trạng thái mở / đóng cửa")
    private Boolean isActive;
}

