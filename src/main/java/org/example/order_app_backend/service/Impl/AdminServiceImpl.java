package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.response.DisputeResponse;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.entity.DisputeEntity;
import org.example.order_app_backend.entity.OrderEntity;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.DisputeMapper;
import org.example.order_app_backend.mapper.UserMapper;
import org.example.order_app_backend.repository.DisputeRepository;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DisputeMapper disputeMapper;
    private final UserMapper userMapper;

    @Override
    public List<DisputeResponse> getAllDisputes() {
        List<DisputeEntity> disputes = disputeRepository.findAll();
        return disputes.stream().map(dispute -> {
            String orderCode = orderRepository.findById(dispute.getOrderId())
                    .map(OrderEntity::getOrderCode)
                    .orElse("KTX-ORDER");
            return disputeMapper.toResponse(dispute, orderCode);
        }).toList();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<UserEntity> users = userRepository.findByRole(UserRole.STUDENT);
        if (users.isEmpty()) {
            return List.of();
        }
        return users.stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, String roleName) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        try {
            UserRole targetRole = UserRole.valueOf(roleName.toUpperCase());
            user.setRole(targetRole);
            UserEntity saved = userRepository.save(user);
            log.info("Admin cập nhật role cho user {} sang: {}", user.getEmail(), targetRole);
            return userMapper.toResponse(saved);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
    }
}