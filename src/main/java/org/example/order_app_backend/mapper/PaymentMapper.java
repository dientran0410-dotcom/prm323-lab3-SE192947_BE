package org.example.order_app_backend.mapper;

import org.example.order_app_backend.dto.response.PaymentResponse;
import org.example.order_app_backend.entity.PaymentEntity;
import org.example.order_app_backend.service.gateway.PaymentQrResult;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(PaymentEntity entity, String orderCode, PaymentQrResult qrResult) {
        if (entity == null) {
            return null;
        }

        PaymentResponse response = PaymentResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .orderCode(orderCode)
                .paymentMethod(entity.getPaymentMethod())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .transactionRef(entity.getTransactionRef())
                .vietqrContent(entity.getVietqrContent())
                .qrUrl(entity.getQrUrl())
                .paidAt(entity.getPaidAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        if (qrResult != null) {
            response.setBankBin(qrResult.getBankBin());
            response.setBankName(qrResult.getBankName());
            response.setAccountNumber(qrResult.getAccountNumber());
            response.setAccountName(qrResult.getAccountName());
        }

        return response;
    }
}

