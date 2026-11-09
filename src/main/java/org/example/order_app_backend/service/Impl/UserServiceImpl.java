package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.request.LoginRequest;
import org.example.order_app_backend.dto.request.SyncUserRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.dto.response.UserResponse;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.exception.ResourceNotFoundException;
import org.example.order_app_backend.mapper.UserMapper;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CurrentUserProvider currentUserProvider;
    private final org.example.order_app_backend.service.OtpService otpService;

    @Override
    @Transactional
    public UserResponse syncUser(SyncUserRequest request) {
        log.info("Bắt đầu đồng bộ thông tin người dùng từ Firebase Auth UID: {}", request.getFirebaseUid());

        // 1. Tìm người dùng theo Firebase UID
        Optional<UserEntity> userByUid = userRepository.findByFirebaseUid(request.getFirebaseUid());
        if (userByUid.isPresent()) {
            UserEntity existingUser = userByUid.get();
            userMapper.updateEntityFromSync(existingUser, request);
            UserEntity saved = userRepository.save(existingUser);
            log.info("Cập nhật thành công người dùng ID: {} theo Firebase UID: {}", saved.getId(), request.getFirebaseUid());
            return userMapper.toResponse(saved);
        }

        // 2. Tìm người dùng theo Email (liên kết tài khoản đã tồn tại)
        Optional<UserEntity> userByEmail = userRepository.findByEmail(request.getEmail().toLowerCase().trim());
        if (userByEmail.isPresent()) {
            UserEntity existingUser = userByEmail.get();
            userMapper.updateEntityFromSync(existingUser, request);
            UserEntity saved = userRepository.save(existingUser);
            log.info("Liên kết Firebase UID {} vào tài khoản Email: {} (ID: {})", request.getFirebaseUid(), request.getEmail(), saved.getId());
            return userMapper.toResponse(saved);
        }

        // 3. Tạo mới hoàn toàn nếu chưa từng tồn tại
        UserEntity newUser = userMapper.toEntity(request);
        UserEntity saved = userRepository.save(newUser);
        log.info("Đăng ký thành công sinh viên mới ID: {}, Email: {}", saved.getId(), saved.getEmail());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        String identifier = request.getEmailOrPhone().trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài khoản với email/SĐT: " + identifier));

        if (Boolean.TRUE.equals(user.getIsBanned())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tài khoản của bạn đã bị khóa do vi phạm chính sách hoặc khiếu nại. Vui lòng liên hệ Ban quản lý KTX.");
        }

        log.info("Người dùng {} (Vai trò: {}) đăng nhập thành công qua mật khẩu", user.getDisplayName(), user.getRole());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        if (!currentUserProvider.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn cần đăng nhập để truy cập thông tin cá nhân");
        }

        Long userId = currentUserProvider.getCurrentUserId();
        String firebaseUid = currentUserProvider.getCurrentUserFirebaseUid();

        UserEntity user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null && firebaseUid != null) {
            user = userRepository.findByFirebaseUid(firebaseUid).orElse(null);
        }

        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin hồ sơ của người dùng hiện tại");
        }

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateUserProfileRequest request) {
        if (!currentUserProvider.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn cần đăng nhập để cập nhật thông tin cá nhân");
        }

        Long userId = currentUserProvider.getCurrentUserId();
        String firebaseUid = currentUserProvider.getCurrentUserFirebaseUid();

        UserEntity user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null && firebaseUid != null) {
            user = userRepository.findByFirebaseUid(firebaseUid).orElse(null);
        }

        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng để cập nhật thông tin");
        }

        userMapper.updateEntityFromRequest(user, request);
        UserEntity saved = userRepository.save(user);
        log.info("Cập nhật thành công hồ sơ người dùng ID: {}", saved.getId());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse resetPassword(org.example.order_app_backend.dto.request.ResetPasswordRequest request) {
        String cleanPhone = request.getPhoneNumber().replaceAll("[^0-9]", "").trim();
        log.info("Bắt đầu xử lý đặt lại mật khẩu cho số điện thoại: {}", cleanPhone);

        // 1. Xác thực mã OTP
        boolean isOtpValid = otpService.verifyOtp(cleanPhone, request.getOtp());
        if (!isOtpValid) {
            log.warn("Xác thực OTP thất bại cho số điện thoại: {}, mã nhập: {}", cleanPhone, request.getOtp());
            throw new AppException(ErrorCode.INVALID_OTP, "Mã xác thực OTP không chính xác hoặc đã hết hạn. Vui lòng kiểm tra lại terminal hoặc gửi lại mã mới.");
        }

        // 2. Tìm người dùng theo số điện thoại
        UserEntity user = userRepository.findByPhone(cleanPhone)
                .or(() -> userRepository.findByEmail(cleanPhone))
                .orElse(null);

        if (user == null) {
            // Nếu là số điện thoại mới chưa có trong hệ thống, tự động tạo tài khoản sinh viên demo
            user = UserEntity.builder()
                    .phone(cleanPhone)
                    .email(cleanPhone + "@student.fpt.edu.vn")
                    .displayName("Sinh viên KTX (" + (cleanPhone.length() >= 4 ? cleanPhone.substring(cleanPhone.length() - 4) : cleanPhone) + ")")
                    .role(org.example.order_app_backend.entity.UserRole.STUDENT)
                    .password(request.getNewPassword().trim())
                    .build();
            log.info("Tạo mới tài khoản sinh viên với mật khẩu mới cho SĐT: {}", cleanPhone);
        } else {
            user.setPassword(request.getNewPassword().trim());
            log.info("Cập nhật mật khẩu mới thành công cho người dùng ID: {}, SĐT: {}", user.getId(), cleanPhone);
        }

        UserEntity saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }
}
