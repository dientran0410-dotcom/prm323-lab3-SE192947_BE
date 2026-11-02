package org.example.order_app_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.order_app_backend.entity.PaymentMethod;
import org.example.order_app_backend.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private String orderCode;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionRef;
    private String vietqrContent;
    private String qrUrl;
    private String bankBin;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
}

