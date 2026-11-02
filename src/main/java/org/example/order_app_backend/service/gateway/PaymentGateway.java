package org.example.order_app_backend.service.gateway;

import org.example.order_app_backend.entity.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentMethod getSupportedMethod();

    PaymentQrResult generatePaymentQr(Long orderId, String orderCode, BigDecimal amount);
}

