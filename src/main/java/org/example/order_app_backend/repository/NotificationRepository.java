package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationRepository {
    NotificationEntity save(NotificationEntity notification);
    Optional<NotificationEntity> findById(Long id);
    Page<NotificationEntity> findByUserId(Long userId, Pageable pageable);
    long countByUserIdAndIsReadFalse(Long userId);
    void markAllAsReadByUserId(Long userId);
}

