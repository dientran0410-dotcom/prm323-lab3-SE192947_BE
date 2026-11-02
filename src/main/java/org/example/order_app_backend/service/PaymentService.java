package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse generatePaymentQr(Long orderId);

    PaymentResponse switchToCod(Long orderId);

    PaymentResponse getPaymentByOrderId(Long orderId);

    PaymentResponse confirmPaymentSuccess(Long orderId, String transactionRef);
}

