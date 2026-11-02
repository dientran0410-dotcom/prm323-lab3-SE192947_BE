package org.example.order_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.SyncUserRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.example.order_app_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserEntity seededUser;

    @BeforeEach
    void setUp() {
        // Khởi tạo một người dùng trong DB test
        if (userRepository.findByFirebaseUid("fb_auth_test_999").isEmpty()) {
            seededUser = userRepository.save(UserEntity.builder()
                    .firebaseUid("fb_auth_test_999")
                    .email("datnh@fpt.edu.vn")
                    .displayName("Ngô Hoàng Trường Đạt")
                    .phone("0909123456")
                    .role(UserRole.STUDENT)
                    .ktxBuilding("Khu B - Tòa BA1")
                    .ktxRoom("201")
                    .build());
        } else {
            seededUser = userRepository.findByFirebaseUid("fb_auth_test_999").get();
            seededUser.setKtxBuilding("Khu B - Tòa BA1");
            seededUser.setKtxRoom("201");
            seededUser.setDisplayName("Ngô Hoàng Trường Đạt");
            seededUser = userRepository.save(seededUser);
        }
    }

    @Test
    @DisplayName("POST /api/v1/auth/sync-user - Happy Path: Đồng bộ thành công người dùng mới")
    void syncUser_ValidPayload_ShouldReturnSuccess() throws Exception {
        SyncUserRequest request = SyncUserRequest.builder()
                .firebaseUid("fb_new_student_888")
                .email("vinhqn@fpt.edu.vn")
                .displayName("Quách Ngọc Vinh")
                .phone("0933445566")
                .ktxBuilding("Khu B - Tòa BA3")
                .ktxRoom("502")
                .build();

        mockMvc.perform(post("/api/v1/auth/sync-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firebaseUid").value("fb_new_student_888"))
                .andExpect(jsonPath("$.data.email").value("vinhqn@fpt.edu.vn"))
                .andExpect(jsonPath("$.data.displayName").value("Quách Ngọc Vinh"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/sync-user - Validation Error: Thiếu UID và sai định dạng email trả về HTTP 400")
    void syncUser_InvalidPayload_ShouldReturnBadRequest() throws Exception {
        SyncUserRequest request = SyncUserRequest.builder()
                .firebaseUid("") // Blank UID
                .email("not-an-email") // Invalid email
                .displayName("") // Blank name
                .build();

        mockMvc.perform(post("/api/v1/auth/sync-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Happy Path: Có header xác thực trả về hồ sơ cá nhân")
    void getCurrentUser_WithAuthHeader_ShouldReturnProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-User-Id", seededUser.getId())
                        .header("X-Firebase-Uid", seededUser.getFirebaseUid())
                        .header("X-User-Email", seededUser.getEmail())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("datnh@fpt.edu.vn"))
                .andExpect(jsonPath("$.data.displayName").value("Ngô Hoàng Trường Đạt"))
                .andExpect(jsonPath("$.data.ktxRoom").value("201"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Unauthorized: Không có thông tin xác thực trả về HTTP 401")
    void getCurrentUser_NoAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me - Happy Path: Cập nhật phòng KTX thành công")
    void updateProfile_WithAuthHeader_ShouldUpdateKtxInfo() throws Exception {
        UpdateUserProfileRequest updateRequest = UpdateUserProfileRequest.builder()
                .ktxBuilding("Khu B - Tòa BA2")
                .ktxRoom("305")
                .build();

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("X-User-Id", seededUser.getId())
                        .header("X-Firebase-Uid", seededUser.getFirebaseUid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ktxBuilding").value("Khu B - Tòa BA2"))
                .andExpect(jsonPath("$.data.ktxRoom").value("305"));
    }
}
