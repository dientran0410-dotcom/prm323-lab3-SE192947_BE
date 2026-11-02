package org.example.order_app_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu kiểm thử cơ chế Validation tầng Foundation")
public class HealthValidationRequest {

    @NotBlank(message = "Tên không được để trống")
    @Schema(description = "Tên người dùng", example = "Trần Sinh Viên")
    private String name;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    @Schema(description = "Email sinh viên", example = "student@fpt.edu.vn")
    private String email;

    @Positive(message = "Số lượng phải lớn hơn 0")
    @Schema(description = "Số lượng đặt thử", example = "2")
    private Integer quantity;
}
