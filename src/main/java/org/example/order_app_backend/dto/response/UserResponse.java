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
@Schema(description = "Thông tin chi tiết người dùng / sinh viên")
public class UserResponse {

    @Schema(description = "ID nội bộ trong hệ thống", example = "1")
    private Long id;

    @Schema(description = "UID Firebase Authentication", example = "firebase_user_abc123")
    private String firebaseUid;

    @Schema(description = "Email tài khoản", example = "sinhvien@fpt.edu.vn")
    private String email;

    @Schema(description = "Họ tên hiển thị", example = "Bùi Bảo Dương")
    private String displayName;

    @Schema(description = "Số điện thoại", example = "0987654321")
    private String phone;

    @Schema(description = "Vai trò trong hệ thống", example = "STUDENT")
    private String role;

    @Schema(description = "Ảnh đại diện", example = "https://lh3.googleusercontent.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Tòa KTX", example = "Khu B - Tòa BA1")
    private String ktxBuilding;

    @Schema(description = "Số phòng KTX", example = "312")
    private String ktxRoom;

    @Schema(description = "Điểm đánh giá trung bình (dành cho Shipper)", example = "4.9")
    private BigDecimal ratingAvg;

    @Schema(description = "Tổng số lượt đánh giá (dành cho Shipper)", example = "25")
    private Integer ratingCount;

    @Schema(description = "Trạng thái khóa tài khoản / ban bởi Admin", example = "false")
    private Boolean isBanned;

    @Schema(description = "Thời gian đăng ký tài khoản")
    private Instant createdAt;

    @Schema(description = "Thời gian cập nhật hồ sơ gần nhất")
    private Instant updatedAt;
}
