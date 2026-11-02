package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    ReviewEntity save(ReviewEntity review);

    Optional<ReviewEntity> findById(Long id);

    Optional<ReviewEntity> findByOrderId(Long orderId);

    List<ReviewEntity> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    Page<ReviewEntity> findByStoreId(Long storeId, Pageable pageable);

    boolean existsByOrderId(Long orderId);
}

