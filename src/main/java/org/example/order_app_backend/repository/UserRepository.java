package org.example.order_app_backend.repository;

import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    UserEntity save(UserEntity user);

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByFirebaseUid(String firebaseUid);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    List<UserEntity> findByRole(UserRole role);

    long count();
}
