package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.DisputeEntity;
import org.example.order_app_backend.repository.DisputeRepository;
import org.example.order_app_backend.repository.mysql.jpa.DisputeJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlDisputeRepository implements DisputeRepository {

    private final DisputeJpaRepository jpaRepository;

    @Override
    public DisputeEntity save(DisputeEntity dispute) {
        return jpaRepository.save(dispute);
    }

    @Override
    public Optional<DisputeEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<DisputeEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<DisputeEntity> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<DisputeEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return jpaRepository.existsByOrderId(orderId);
    }
}

