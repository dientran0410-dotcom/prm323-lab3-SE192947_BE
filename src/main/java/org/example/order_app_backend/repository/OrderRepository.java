package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    OrderEntity save(OrderEntity order);

    Optional<OrderEntity> findById(Long id);

    Optional<OrderEntity> findByOrderCode(String orderCode);

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    Page<OrderEntity> findByStoreId(Long storeId, Pageable pageable);

    List<OrderEntity> findAll();

    boolean existsById(Long id);

    List<OrderEntity> findByStoreIdAndSlotTimeOrderByCreatedAtDesc(Long storeId, String slotTime);

    List<OrderEntity> findBySlotTimeAndStatusInOrderByCreatedAtAsc(String slotTime, List<org.example.order_app_backend.entity.OrderStatus> statuses);

    List<OrderEntity> findByShipperIdOrderByCreatedAtDesc(Long shipperId);
}

