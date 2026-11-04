package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.StoreEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StoreRepository {

    StoreEntity save(StoreEntity store);

    Optional<StoreEntity> findById(Long id);

    List<StoreEntity> findAll();

    Page<StoreEntity> searchStores(String keyword, String campusZone, Boolean isActive, Pageable pageable);

    boolean existsById(Long id);

    List<StoreEntity> findByOwnerId(Long ownerId);
}
