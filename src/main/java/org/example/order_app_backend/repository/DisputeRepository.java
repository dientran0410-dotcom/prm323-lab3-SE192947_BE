package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.DisputeEntity;

import java.util.List;
import java.util.Optional;

public interface DisputeRepository {

    DisputeEntity save(DisputeEntity dispute);

    Optional<DisputeEntity> findById(Long id);

    Optional<DisputeEntity> findByOrderId(Long orderId);

    List<DisputeEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<DisputeEntity> findAll();

    boolean existsByOrderId(Long orderId);
}

