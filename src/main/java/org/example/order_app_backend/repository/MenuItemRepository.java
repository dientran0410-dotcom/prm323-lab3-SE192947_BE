package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.MenuItemEntity;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository {

    MenuItemEntity save(MenuItemEntity item);

    Optional<MenuItemEntity> findById(Long id);

    List<MenuItemEntity> findByIdIn(List<Long> ids);

    List<MenuItemEntity> findByStoreId(Long storeId);

    List<MenuItemEntity> findByStoreIdAndCategory(Long storeId, String category);

    List<MenuItemEntity> findByStoreIdAndIsAvailable(Long storeId, Boolean isAvailable);

    List<MenuItemEntity> findByStoreIdAndCategoryAndIsAvailable(Long storeId, String category, Boolean isAvailable);

    boolean existsById(Long id);

    void deleteById(Long id);
}

