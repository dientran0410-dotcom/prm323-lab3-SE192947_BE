package org.example.order_app_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đồng bộ tài khoản sau khi đăng nhập qua Google Sign-In hoặc SĐT")
public class SyncUserRequest {

    @NotBlank(message = "Firebase UID không được để trống")
    @Schema(description = "UID do Firebase Auth cấp", example = "firebase_user_abc123")
    private String firebaseUid;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    @Schema(description = "Email người dùng", example = "sinhvien@fpt.edu.vn")
    private String email;

    @NotBlank(message = "Họ tên hiển thị không được để trống")
    @Schema(description = "Họ tên sinh viên", example = "Bùi Bảo Dương")
    private String displayName;

    @Schema(description = "Số điện thoại liên hệ", example = "0987654321")
    private String phone;

    @Schema(description = "Đường dẫn ảnh đại diện", example = "https://lh3.googleusercontent.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Tòa KTX sinh viên đang ở", example = "Khu B - Tòa BA1")
    private String ktxBuilding;

    @Schema(description = "Số phòng KTX", example = "312")
    private String ktxRoom;
}

