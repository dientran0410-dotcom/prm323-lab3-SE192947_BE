package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.MenuItemEntity;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.mysql.jpa.MenuItemJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlMenuItemRepository implements MenuItemRepository {

    private final MenuItemJpaRepository jpaRepository;

    @Override
    public MenuItemEntity save(MenuItemEntity item) {
        return jpaRepository.save(item);
    }

    @Override
    public Optional<MenuItemEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<MenuItemEntity> findByIdIn(List<Long> ids) {
        return jpaRepository.findByIdIn(ids);
    }

    @Override
    public List<MenuItemEntity> findByStoreId(Long storeId) {
        return jpaRepository.findByStoreId(storeId);
    }

    @Override
    public List<MenuItemEntity> findByStoreIdAndCategory(Long storeId, String category) {
        return jpaRepository.findByStoreIdAndCategory(storeId, category);
    }

    @Override
    public List<MenuItemEntity> findByStoreIdAndIsAvailable(Long storeId, Boolean isAvailable) {
        return jpaRepository.findByStoreIdAndIsAvailable(storeId, isAvailable);
    }

    @Override
    public List<MenuItemEntity> findByStoreIdAndCategoryAndIsAvailable(Long storeId, String category, Boolean isAvailable) {
        return jpaRepository.findByStoreIdAndCategoryAndIsAvailable(storeId, category, isAvailable);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}

