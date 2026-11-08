package org.example.order_app_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.order_app_backend.entity.PaymentMethod;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "ID quán ăn không được để trống")
    private Long storeId;

    @NotBlank(message = "Ca gom đơn KTX không được để trống")
    private String slotTime;

    @NotBlank(message = "Điểm giao nhận KTX không được để trống")
    private String pickupLocation;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    private String note;

    private String voucherCode;

    private java.math.BigDecimal groupDiscountAmount;

    @NotEmpty(message = "Đơn hàng phải chứa ít nhất một món ăn")
    @Valid
    private List<OrderItemRequest> items;
}

