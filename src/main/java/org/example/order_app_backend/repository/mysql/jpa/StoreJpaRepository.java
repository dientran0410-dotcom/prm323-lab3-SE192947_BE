package org.example.order_app_backend.repository.mysql.jpa;

import org.example.order_app_backend.entity.StoreEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreJpaRepository extends JpaRepository<StoreEntity, Long> {

    @Query("SELECT s FROM StoreEntity s WHERE " +
           "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:campusZone IS NULL OR s.campusZone = :campusZone) " +
           "AND (:isActive IS NULL OR s.isActive = :isActive)")
    Page<StoreEntity> searchStores(
            @Param("keyword") String keyword,
            @Param("campusZone") String campusZone,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    List<StoreEntity> findByOwnerId(Long ownerId);
}
