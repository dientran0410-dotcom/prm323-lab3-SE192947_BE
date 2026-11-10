package org.example.order_app_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Lựa chọn tùy biến cụ thể (vd: Size L, Trứng ốp la)")
public class CustomizationOptionDto {

    @JsonAlias({"name", "option", "choice"})
    @Schema(description = "Tên tùy chọn", example = "Trứng ốp la")
    private String name;

    @JsonAlias({"extraPrice", "price"})
    @Schema(description = "Phụ thu (VND)", example = "6000.00")
    @Builder.Default
    private BigDecimal extraPrice = BigDecimal.ZERO;
}


