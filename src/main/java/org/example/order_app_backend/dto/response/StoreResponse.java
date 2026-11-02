package org.example.order_app_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết quán ăn")
public class StoreResponse {

    @Schema(description = "ID quán ăn", example = "1")
    private Long id;

    @Schema(description = "Tên quán ăn", example = "Cơm Tấm Sườn Bì KTX B2")
    private String name;

    @Schema(description = "Mô tả quán ăn", example = "Cơm tấm nóng sốt, phục vụ tận trụ KTX")
    private String description;

    @Schema(description = "Địa chỉ thực tế", example = "Cổng B2 KTX ĐHQG")
    private String address;

    @Schema(description = "Phân khu KTX", example = "KTX_KHU_B")
    private String campusZone;

    @Schema(description = "Số điện thoại hotline", example = "0987123456")
    private String phoneNumber;

    @Schema(description = "Ảnh đại diện", example = "https://img.food.vn/comtam.jpg")
    private String avatarUrl;

    @Schema(description = "Trạng thái đang mở cửa", example = "true")
    private Boolean isActive;

    @Schema(description = "Điểm đánh giá trung bình", example = "4.80")
    private BigDecimal ratingAvg;

    @Schema(description = "Tổng số lượt đánh giá", example = "128")
    private Integer ratingCount;

    @Schema(description = "Thời gian tạo")
    private Instant createdAt;

    @Schema(description = "Thời gian cập nhật")
    private Instant updatedAt;
}

