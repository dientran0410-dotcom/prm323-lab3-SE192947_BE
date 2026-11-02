package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.NotificationEntity;
import org.example.order_app_backend.repository.NotificationRepository;
import org.example.order_app_backend.repository.mysql.jpa.NotificationJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlNotificationRepository implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public NotificationEntity save(NotificationEntity notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public Optional<NotificationEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<NotificationEntity> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long countByUserIdAndIsReadFalse(Long userId) {
        return jpaRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAllAsReadByUserId(Long userId) {
        jpaRepository.markAllAsReadByUserId(userId);
    }
}
