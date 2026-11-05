package org.example.order_app_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.order_app_backend.entity.DisputeReason;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Builder.Default
    private List<String> evidenceImages = new ArrayList<>();

    @JsonProperty("evidenceImageUrl")
    public void setEvidenceImageUrl(String evidenceImageUrl) {
        if (evidenceImageUrl != null && !evidenceImageUrl.isBlank()) {
            if (this.evidenceImages == null) {
                this.evidenceImages = new ArrayList<>();
            }
            if (!this.evidenceImages.contains(evidenceImageUrl)) {
                this.evidenceImages.add(evidenceImageUrl);
            }
        }
    }

    @DecimalMin(value = "1.0", message = "Số tiền yêu cầu hoàn phải lớn hơn 0")
    @JsonAlias("requestedRefundAmount")
    private BigDecimal refundRequestedAmount;
}

