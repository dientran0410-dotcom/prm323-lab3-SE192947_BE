package org.example.order_app_backend.repository.mysql.jpa;

import org.example.order_app_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByFirebaseUid(String firebaseUid);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);
}

