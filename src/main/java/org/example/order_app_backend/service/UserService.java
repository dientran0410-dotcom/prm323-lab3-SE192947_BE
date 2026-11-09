package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.LoginRequest;
import org.example.order_app_backend.dto.request.SyncUserRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.dto.response.UserResponse;

public interface UserService {

    UserResponse syncUser(SyncUserRequest request);

    UserResponse login(LoginRequest request);

    UserResponse getCurrentUserProfile();

    UserResponse getUserById(Long id);

    UserResponse updateProfile(UpdateUserProfileRequest request);

    UserResponse resetPassword(org.example.order_app_backend.dto.request.ResetPasswordRequest request);
}
