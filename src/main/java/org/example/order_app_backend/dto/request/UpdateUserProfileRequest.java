package org.example.order_app_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật hồ sơ sinh viên KTX")
public class UpdateUserProfileRequest {

    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    @Schema(description = "Họ tên hiển thị", example = "Trần Sanh Điền")
    private String displayName;

    @Size(max = 20, message = "Số điện thoại không hợp lệ")
    @Schema(description = "Số điện thoại nhận hàng", example = "0912345678")
    private String phone;

    @Schema(description = "Đường dẫn ảnh đại diện mới", example = "https://img.fpt.edu.vn/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Tòa KTX sinh viên đang ở", example = "Khu B - Tòa BA2")
    private String ktxBuilding;

    @Schema(description = "Số phòng KTX", example = "405")
    private String ktxRoom;
}

