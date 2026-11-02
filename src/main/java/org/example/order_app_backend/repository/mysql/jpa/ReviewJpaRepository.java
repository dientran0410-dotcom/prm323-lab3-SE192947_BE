package org.example.order_app_backend.repository.mysql.jpa;

import org.example.order_app_backend.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {

    Optional<ReviewEntity> findByOrderId(Long orderId);

    List<ReviewEntity> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    Page<ReviewEntity> findByStoreId(Long storeId, Pageable pageable);

    boolean existsByOrderId(Long orderId);
}

