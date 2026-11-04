package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.response.DisputeResponse;
import org.example.order_app_backend.dto.response.UserResponse;

import java.util.List;

public interface AdminService {

    List<DisputeResponse> getAllDisputes();

    List<UserResponse> getAllUsers();

    UserResponse updateUserRole(Long userId, String role);
}