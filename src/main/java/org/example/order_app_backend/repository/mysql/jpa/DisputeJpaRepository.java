package org.example.order_app_backend.repository.mysql.jpa;

import org.example.order_app_backend.entity.DisputeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeJpaRepository extends JpaRepository<DisputeEntity, Long> {

    Optional<DisputeEntity> findByOrderId(Long orderId);

    List<DisputeEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByOrderId(Long orderId);
}

