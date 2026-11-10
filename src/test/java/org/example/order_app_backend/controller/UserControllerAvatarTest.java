package org.example.order_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.UpdateAvatarRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.example.order_app_backend.mapper.UserMapper;
import org.example.order_app_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerAvatarTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserEntity studentUser;
    private UserEntity shipperUser;
    private UserEntity storeOwnerUser;
    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        studentUser = getOrCreateUser("student_avatar_test@fpt.edu.vn", "Student Avatar Tester", UserRole.STUDENT, "fb_student_avatar_01");
        shipperUser = getOrCreateUser("shipper_avatar_test@fpt.edu.vn", "Shipper Avatar Tester", UserRole.SHIPPER, "fb_shipper_avatar_01");
        storeOwnerUser = getOrCreateUser("owner_avatar_test@fpt.edu.vn", "Store Owner Avatar Tester", UserRole.STORE_OWNER, "fb_owner_avatar_01");
        adminUser = getOrCreateUser("admin_avatar_test@fpt.edu.vn", "Admin Avatar Tester", UserRole.ADMIN, "fb_admin_avatar_01");
    }

    private UserEntity getOrCreateUser(String email, String name, UserRole role, String firebaseUid) {
        return userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email(email)
                        .displayName(name)
                        .phone("09" + Math.abs(email.hashCode() % 100000000))
                        .role(role)
                        .firebaseUid(firebaseUid)
                        .ktxBuilding("Khu B - Tòa BA1")
                        .ktxRoom("101")
                        .build())
        );
    }

    @Test
    @DisplayName("Avatar CRUD - 1. Role STUDENT: Tải lên avatar multipart PNG thành công")
    void testUploadAvatar_Student_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "student_avatar.png", MediaType.IMAGE_PNG_VALUE, "mock-png-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(file)
                        .header("X-User-Id", studentUser.getId())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl", notNullValue()))
                .andExpect(jsonPath("$.data.avatarUrl", containsString("food_ordering/avatars")));
    }

    @Test
    @DisplayName("Avatar CRUD - 2. Role SHIPPER: Tải lên avatar multipart JPEG thành công")
    void testUploadAvatar_Shipper_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "shipper_avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "mock-jpeg-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(file)
                        .header("X-User-Id", shipperUser.getId())
                        .header("X-User-Role", "SHIPPER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl", notNullValue()))
                .andExpect(jsonPath("$.data.avatarUrl", containsString("food_ordering/avatars")));
    }

    @Test
    @DisplayName("Avatar CRUD - 3. Role STORE_OWNER: Tải lên avatar multipart WEBP thành công")
    void testUploadAvatar_StoreOwner_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "store_avatar.webp", "image/webp", "mock-webp-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(file)
                        .header("X-User-Id", storeOwnerUser.getId())
                        .header("X-User-Role", "STORE_OWNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl", notNullValue()))
                .andExpect(jsonPath("$.data.avatarUrl", containsString("food_ordering/avatars")));
    }

    @Test
    @DisplayName("Avatar CRUD - 4. Role ADMIN: Tải lên avatar multipart PNG thành công")
    void testUploadAvatar_Admin_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "admin_avatar.png", MediaType.IMAGE_PNG_VALUE, "mock-admin-png-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(file)
                        .header("X-User-Id", adminUser.getId())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl", notNullValue()))
                .andExpect(jsonPath("$.data.avatarUrl", containsString("food_ordering/avatars")));
    }

    @Test
    @DisplayName("Avatar CRUD - 5. GET /api/v1/users/me/avatar trả về avatarUrl")
    void testGetAvatar_Success() throws Exception {
        studentUser.setAvatarUrl("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/food_ordering/avatars/user_student_get.png");
        userRepository.save(studentUser);

        mockMvc.perform(get("/api/v1/users/me/avatar")
                        .header("X-User-Id", studentUser.getId())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").value(studentUser.getAvatarUrl()));
    }

    @Test
    @DisplayName("Avatar CRUD - 6. PUT /api/v1/users/me/avatar cập nhật avatarUrl trực tiếp")
    void testPutAvatarUrl_Success() throws Exception {
        UpdateAvatarRequest request = new UpdateAvatarRequest("https://res.cloudinary.com/mnzb5t1m/image/upload/v2/food_ordering/avatars/new_url.png");

        mockMvc.perform(put("/api/v1/users/me/avatar")
                        .header("X-User-Id", studentUser.getId())
                        .header("X-User-Role", "STUDENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").value(request.getAvatarUrl()));
    }

    @Test
    @DisplayName("Avatar CRUD - 7. DELETE /api/v1/users/me/avatar đặt avatarUrl về null")
    void testDeleteAvatar_Success() throws Exception {
        studentUser.setAvatarUrl("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/food_ordering/avatars/to_delete.png");
        userRepository.save(studentUser);

        mockMvc.perform(delete("/api/v1/users/me/avatar")
                        .header("X-User-Id", studentUser.getId())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

        // Sau khi xóa, GET cũng trả về avatarUrl = null
        mockMvc.perform(get("/api/v1/users/me/avatar")
                        .header("X-User-Id", studentUser.getId())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));
    }

    @Test
    @DisplayName("Avatar CRUD - 8. Admin POST /api/v1/admin/users/{id}/avatar tải avatar cho user")
    void testAdminUploadUserAvatar_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "admin_assigned.png", MediaType.IMAGE_PNG_VALUE, "admin-upload-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/admin/users/" + studentUser.getId() + "/avatar")
                        .file(file)
                        .header("X-User-Id", adminUser.getId())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(studentUser.getId()))
                .andExpect(jsonPath("$.data.avatarUrl", notNullValue()));
    }

    @Test
    @DisplayName("Avatar CRUD - 9. Admin DELETE /api/v1/admin/users/{id}/avatar xóa avatar của user")
    void testAdminDeleteUserAvatar_Success() throws Exception {
        studentUser.setAvatarUrl("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/food_ordering/avatars/admin_remove.png");
        userRepository.save(studentUser);

        mockMvc.perform(delete("/api/v1/admin/users/" + studentUser.getId() + "/avatar")
                        .header("X-User-Id", adminUser.getId())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(studentUser.getId()))
                .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));
    }

    @Test
    @DisplayName("Avatar Validation - 10. Từ chối định dạng tập tin không hợp lệ (PDF)")
    void testUploadAvatar_RejectInvalidMimeType_Pdf() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "%PDF-1.4 mock pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(pdfFile)
                        .header("X-User-Id", studentUser.getId())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("Avatar Validation - 11. Từ chối định dạng tập tin không hợp lệ (TXT)")
    void testUploadAvatar_RejectInvalidMimeType_Txt() throws Exception {
        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "note.txt", "text/plain", "plain text content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(txtFile)
                        .header("X-User-Id", studentUser.getId())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("Avatar Validation - 12. Từ chối tập tin rỗng")
    void testUploadAvatar_RejectEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(emptyFile)
                        .header("X-User-Id", studentUser.getId())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("UserMapper - 13. updateEntityFromRequest cho phép đặt avatarUrl về null khi rỗng")
    void testUserMapper_ResetAvatarUrl_WhenEmpty() {
        UserEntity user = UserEntity.builder()
                .displayName("Nguyen Van A")
                .avatarUrl("https://res.cloudinary.com/mnzb5t1m/old_avatar.png")
                .build();

        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                .avatarUrl("") // Yêu cầu xóa avatar bằng chuỗi rỗng
                .build();

        userMapper.updateEntityFromRequest(user, request);
        assertNull(user.getAvatarUrl());
    }
}
