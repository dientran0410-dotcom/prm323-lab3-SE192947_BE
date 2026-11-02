package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.PaymentEntity;
import org.example.order_app_backend.repository.PaymentRepository;
import org.example.order_app_backend.repository.mysql.jpa.PaymentJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlPaymentRepository implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public PaymentEntity save(PaymentEntity payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<PaymentEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<PaymentEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<PaymentEntity> findByTransactionRef(String transactionRef) {
        return jpaRepository.findByTransactionRef(transactionRef);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return jpaRepository.existsByOrderId(orderId);
    }
}

