package org.example.order_app_backend.repository.mysql;

import lombok.RequiredArgsConstructor;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.repository.mysql.jpa.UserJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class MySqlUserRepository implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public UserEntity save(UserEntity user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByFirebaseUid(String firebaseUid) {
        return jpaRepository.findByFirebaseUid(firebaseUid);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<UserEntity> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByFirebaseUid(String firebaseUid) {
        return jpaRepository.existsByFirebaseUid(firebaseUid);
    }
}

