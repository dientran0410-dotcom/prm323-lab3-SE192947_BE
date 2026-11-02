package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.OrderItemEntity;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {

    OrderItemEntity save(OrderItemEntity orderItem);

    List<OrderItemEntity> saveAll(List<OrderItemEntity> orderItems);

    List<OrderItemEntity> findByOrderId(Long orderId);

    Optional<OrderItemEntity> findById(Long id);
}

