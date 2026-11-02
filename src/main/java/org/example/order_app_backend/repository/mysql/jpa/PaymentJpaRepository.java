package org.example.order_app_backend.repository.mysql.jpa;

import org.example.order_app_backend.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);

    Optional<PaymentEntity> findByTransactionRef(String transactionRef);

    boolean existsByOrderId(Long orderId);
}

