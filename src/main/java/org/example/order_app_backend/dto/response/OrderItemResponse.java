package org.example.order_app_backend.dto.response;

import lombok.*;
import org.example.order_app_backend.dto.request.SelectedItemOptionDto;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;
    private Long menuItemId;
    private String itemName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private List<SelectedItemOptionDto> selectedOptions;
    private BigDecimal itemTotalAmount;
}

