package org.example.order_app_backend.repository.mysql.jpa;

import org.example.order_app_backend.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderCode(String orderCode);

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    Page<OrderEntity> findByStoreId(Long storeId, Pageable pageable);
}

