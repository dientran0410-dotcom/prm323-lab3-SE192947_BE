package org.example.order_app_backend.repository.mysql.jpa;

import org.example.order_app_backend.entity.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemJpaRepository extends JpaRepository<MenuItemEntity, Long> {

    List<MenuItemEntity> findByStoreId(Long storeId);

    List<MenuItemEntity> findByStoreIdAndCategory(Long storeId, String category);

    List<MenuItemEntity> findByStoreIdAndIsAvailable(Long storeId, Boolean isAvailable);

    List<MenuItemEntity> findByStoreIdAndCategoryAndIsAvailable(Long storeId, String category, Boolean isAvailable);

    List<MenuItemEntity> findByIdIn(List<Long> ids);
}

