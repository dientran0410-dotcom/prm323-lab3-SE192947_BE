package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.SyncUserRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.dto.response.UserResponse;

public interface UserService {

    UserResponse syncUser(SyncUserRequest request);

    UserResponse getCurrentUserProfile();

    UserResponse getUserById(Long id);

    UserResponse updateProfile(UpdateUserProfileRequest request);
}

