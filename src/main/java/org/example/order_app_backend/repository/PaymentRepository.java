package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.PaymentEntity;

import java.util.Optional;

public interface PaymentRepository {

    PaymentEntity save(PaymentEntity payment);

    Optional<PaymentEntity> findById(Long id);

    Optional<PaymentEntity> findByOrderId(Long orderId);

    Optional<PaymentEntity> findByTransactionRef(String transactionRef);

    boolean existsByOrderId(Long orderId);
}

