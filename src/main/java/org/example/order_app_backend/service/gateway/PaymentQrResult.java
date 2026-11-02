package org.example.order_app_backend.service.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQrResult {

    private String qrUrl;
    private String vietqrContent;
    private String bankBin;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private BigDecimal amount;
}

