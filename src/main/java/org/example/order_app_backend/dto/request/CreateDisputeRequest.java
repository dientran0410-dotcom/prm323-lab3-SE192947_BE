package org.example.order_app_backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.order_app_backend.entity.DisputeReason;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDisputeRequest {

    @NotNull(message = "ID đơn hàng không được để trống")
    private Long orderId;

    @NotNull(message = "Lý do khiếu nại không được để trống")
    private DisputeReason reason;

    @NotBlank(message = "Mô tả chi tiết sự cố không được để trống")
    private String description;

    @NotEmpty(message = "Khiếu nại bắt buộc phải có ít nhất một ảnh bằng chứng")
    private List<String> evidenceImages;

    @NotNull(message = "Số tiền yêu cầu hoàn không được để trống")
    @DecimalMin(value = "1.0", message = "Số tiền yêu cầu hoàn phải lớn hơn 0")
    private BigDecimal refundRequestedAmount;
}

