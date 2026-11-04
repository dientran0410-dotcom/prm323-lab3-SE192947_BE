package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.OrderEntity;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.mysql.jpa.OrderJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public OrderEntity save(OrderEntity order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<OrderEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<OrderEntity> findByOrderCode(String orderCode) {
        return jpaRepository.findByOrderCode(orderCode);
    }

    @Override
    public List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Page<OrderEntity> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<OrderEntity> findByStoreId(Long storeId, Pageable pageable) {
        return jpaRepository.findByStoreId(storeId, pageable);
    }

    @Override
    public List<OrderEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<OrderEntity> findByStoreIdAndSlotTimeOrderByCreatedAtDesc(Long storeId, String slotTime) {
        return jpaRepository.findByStoreIdAndSlotTimeOrderByCreatedAtDesc(storeId, slotTime);
    }

    @Override
    public List<OrderEntity> findBySlotTimeAndStatusInOrderByCreatedAtAsc(String slotTime, List<org.example.order_app_backend.entity.OrderStatus> statuses) {
        return jpaRepository.findBySlotTimeAndStatusInOrderByCreatedAtAsc(slotTime, statuses);
    }

    @Override
    public List<OrderEntity> findByShipperIdOrderByCreatedAtDesc(Long shipperId) {
        return jpaRepository.findByShipperIdOrderByCreatedAtDesc(shipperId);
    }
}

