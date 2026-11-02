package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.StoreEntity;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.repository.mysql.jpa.StoreJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlStoreRepository implements StoreRepository {

    private final StoreJpaRepository jpaRepository;

    @Override
    public StoreEntity save(StoreEntity store) {
        return jpaRepository.save(store);
    }

    @Override
    public Optional<StoreEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<StoreEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Page<StoreEntity> searchStores(String keyword, String campusZone, Boolean isActive, Pageable pageable) {
        return jpaRepository.searchStores(keyword, campusZone, isActive, pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}

