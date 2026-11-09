package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.ReviewEntity;
import org.example.order_app_backend.repository.ReviewRepository;
import org.example.order_app_backend.repository.mysql.jpa.ReviewJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlReviewRepository implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;

    @Override
    public ReviewEntity save(ReviewEntity review) {
        return jpaRepository.save(review);
    }

    @Override
    public Optional<ReviewEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ReviewEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<ReviewEntity> findByStoreIdOrderByCreatedAtDesc(Long storeId) {
        return jpaRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
    }

    @Override
    public Page<ReviewEntity> findByStoreId(Long storeId, Pageable pageable) {
        return jpaRepository.findByStoreId(storeId, pageable);
    }

    @Override
    public List<ReviewEntity> findByShipperIdOrderByCreatedAtDesc(Long shipperId) {
        return jpaRepository.findByShipperIdOrderByCreatedAtDesc(shipperId);
    }

    @Override
    public Page<ReviewEntity> findByShipperId(Long shipperId, Pageable pageable) {
        return jpaRepository.findByShipperId(shipperId, pageable);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return jpaRepository.existsByOrderId(orderId);
    }
}
