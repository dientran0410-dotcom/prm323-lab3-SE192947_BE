package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.OrderItemEntity;
import org.example.order_app_backend.repository.OrderItemRepository;
import org.example.order_app_backend.repository.mysql.jpa.OrderItemJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlOrderItemRepository implements OrderItemRepository {

    private final OrderItemJpaRepository jpaRepository;

    @Override
    public OrderItemEntity save(OrderItemEntity orderItem) {
        return jpaRepository.save(orderItem);
    }

    @Override
    public List<OrderItemEntity> saveAll(List<OrderItemEntity> orderItems) {
        return jpaRepository.saveAll(orderItems);
    }

    @Override
    public List<OrderItemEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<OrderItemEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }
}

