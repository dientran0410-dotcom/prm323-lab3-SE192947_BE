package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.SyncUserRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.exception.ResourceNotFoundException;
import org.example.order_app_backend.mapper.UserMapper;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private UserServiceImpl userService;

    private SyncUserRequest sampleSyncRequest;

    @BeforeEach
    void setUp() {
        sampleSyncRequest = SyncUserRequest.builder()
                .firebaseUid("fb_uid_test_123")
                .email("duongbb@fpt.edu.vn")
                .displayName("Bùi Bảo Dương")
                .phone("0987654321")
                .avatarUrl("https://example.com/avatar.jpg")
                .ktxBuilding("Khu B - Tòa BA1")
                .ktxRoom("312")
                .build();
    }

    @Test
    @DisplayName("syncUser: Người dùng mới hoàn toàn -> Tạo mới bản ghi và trả về UserResponse")
    void syncUser_NewUser_ShouldCreateSuccessfully() {
        when(userRepository.findByFirebaseUid(sampleSyncRequest.getFirebaseUid())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(sampleSyncRequest.getEmail())).thenReturn(Optional.empty());

        UserEntity savedEntity = UserEntity.builder()
                .id(1L)
                .firebaseUid(sampleSyncRequest.getFirebaseUid())
                .email(sampleSyncRequest.getEmail())
                .displayName(sampleSyncRequest.getDisplayName())
                .phone(sampleSyncRequest.getPhone())
                .avatarUrl(sampleSyncRequest.getAvatarUrl())
                .ktxBuilding(sampleSyncRequest.getKtxBuilding())
                .ktxRoom(sampleSyncRequest.getKtxRoom())
                .role(UserRole.STUDENT)
                .build();

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        UserResponse response = userService.syncUser(sampleSyncRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("fb_uid_test_123", response.getFirebaseUid());
        assertEquals("duongbb@fpt.edu.vn", response.getEmail());
        assertEquals("Bùi Bảo Dương", response.getDisplayName());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("syncUser: Đã tồn tại theo Firebase UID -> Cập nhật thông tin")
    void syncUser_ExistingFirebaseUid_ShouldUpdateSuccessfully() {
        UserEntity existingEntity = UserEntity.builder()
                .id(10L)
                .firebaseUid("fb_uid_test_123")
                .email("duongbb@fpt.edu.vn")
                .displayName("Dương Cũ")
                .build();

        when(userRepository.findByFirebaseUid(sampleSyncRequest.getFirebaseUid())).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.syncUser(sampleSyncRequest);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Bùi Bảo Dương", response.getDisplayName());
        verify(userRepository, times(1)).save(existingEntity);
    }

    @Test
    @DisplayName("getCurrentUserProfile: Chưa đăng nhập -> Ném ngoại lệ UNAUTHORIZED")
    void getCurrentUserProfile_Unauthenticated_ShouldThrowAppException() {
        when(currentUserProvider.isAuthenticated()).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> userService.getCurrentUserProfile());
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    @DisplayName("getCurrentUserProfile: Đã đăng nhập và tìm thấy người dùng -> Trả về UserResponse")
    void getCurrentUserProfile_Authenticated_ShouldReturnUser() {
        when(currentUserProvider.isAuthenticated()).thenReturn(true);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        UserEntity user = UserEntity.builder()
                .id(1L)
                .firebaseUid("fb_uid_test_123")
                .email("dien.tran@fpt.edu.vn")
                .displayName("Trần Sanh Điền")
                .role(UserRole.STUDENT)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUserProfile();

        assertNotNull(response);
        assertEquals("Trần Sanh Điền", response.getDisplayName());
        assertEquals("dien.tran@fpt.edu.vn", response.getEmail());
    }

    @Test
    @DisplayName("updateProfile: Cập nhật thành công các trường thông tin KTX")
    void updateProfile_Authenticated_ShouldUpdateFields() {
        when(currentUserProvider.isAuthenticated()).thenReturn(true);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        UserEntity user = UserEntity.builder()
                .id(1L)
                .firebaseUid("fb_uid_test_123")
                .email("dien.tran@fpt.edu.vn")
                .displayName("Trần Sanh Điền")
                .ktxBuilding("Khu B - Tòa BA1")
                .ktxRoom("312")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserProfileRequest updateRequest = UpdateUserProfileRequest.builder()
                .displayName("Trần Sanh Điền - Cập nhật")
                .phone("0911223344")
                .ktxBuilding("Khu B - Tòa BA2")
                .ktxRoom("405")
                .build();

        UserResponse response = userService.updateProfile(updateRequest);

        assertNotNull(response);
        assertEquals("Trần Sanh Điền - Cập nhật", response.getDisplayName());
        assertEquals("0911223344", response.getPhone());
        assertEquals("Khu B - Tòa BA2", response.getKtxBuilding());
        assertEquals("405", response.getKtxRoom());
    }
}

