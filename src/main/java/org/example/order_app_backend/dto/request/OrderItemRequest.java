package org.example.order_app_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotNull(message = "ID món ăn không được để trống")
    private Long menuItemId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng món ăn tối thiểu là 1")
    private Integer quantity;

    @Valid
    @Builder.Default
    private List<SelectedItemOptionDto> selectedOptions = new ArrayList<>();
}

