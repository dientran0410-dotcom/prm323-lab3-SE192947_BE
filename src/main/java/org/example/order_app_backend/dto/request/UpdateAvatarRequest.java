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
@Schema(description = "Yêu cầu cập nhật trực tiếp đường dẫn avatar")
public class UpdateAvatarRequest {

    @Schema(description = "Đường dẫn URL ảnh đại diện", example = "https://res.cloudinary.com/mnzb5t1m/image/upload/v1/food_ordering/avatars/user_1.png")
    private String avatarUrl;
}
