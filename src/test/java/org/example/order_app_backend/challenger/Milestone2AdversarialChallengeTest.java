package org.example.order_app_backend.challenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.UpdateAvatarRequest;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Milestone2AdversarialChallengeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserEntity studentUser;
    private UserEntity shipperUser;
    private UserEntity storeOwnerUser;
    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        studentUser = getOrCreateUser("m2_student@fpt.edu.vn", "Student Challenger", UserRole.STUDENT, "fb_m2_student");
        shipperUser = getOrCreateUser("m2_shipper@fpt.edu.vn", "Shipper Challenger", UserRole.SHIPPER, "fb_m2_shipper");
        storeOwnerUser = getOrCreateUser("m2_owner@fpt.edu.vn", "Owner Challenger", UserRole.STORE_OWNER, "fb_m2_owner");
        adminUser = getOrCreateUser("m2_admin@fpt.edu.vn", "Admin Challenger", UserRole.ADMIN, "fb_m2_admin");
    }

    private UserEntity getOrCreateUser(String email, String name, UserRole role, String firebaseUid) {
        return userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email(email)
                        .displayName(name)
                        .phone("09" + Math.abs(email.hashCode() % 100000000))
                        .role(role)
                        .firebaseUid(firebaseUid)
                        .ktxBuilding("Khu A")
                        .ktxRoom("202")
                        .build())
        );
    }

    private UserEntity getUserForRole(UserRole role) {
        return switch (role) {
            case STUDENT -> studentUser;
            case SHIPPER -> shipperUser;
            case STORE_OWNER -> storeOwnerUser;
            case ADMIN -> adminUser;
        };
    }

    // =========================================================================
    // 1. ADVERSARIAL STRESS TEST: Multi-Role Complete Avatar Lifecycle (CRUD)
    // =========================================================================
    @Nested
    @DisplayName("Challenge Area 1: Multi-Role Avatar CRUD Lifecycle across all 4 roles")
    class MultiRoleAvatarLifecycleChallenge {

        @ParameterizedTest(name = "Role {0} must execute full Avatar CRUD: POST -> GET -> PUT -> DELETE -> GET")
        @EnumSource(UserRole.class)
        void fullAvatarCrudLifecycle_AllRoles(UserRole role) throws Exception {
            UserEntity user = getUserForRole(role);
            String roleName = role.name();

            // 1. POST /api/v1/users/me/avatar (Upload multipart)
            MockMultipartFile file = new MockMultipartFile(
                    "file", "initial_" + roleName.toLowerCase() + ".png", MediaType.IMAGE_PNG_VALUE, "role-avatar-content".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(file)
                            .header("X-User-Id", user.getId())
                            .header("X-User-Role", roleName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl", notNullValue()))
                    .andExpect(jsonPath("$.data.avatarUrl", containsString("food_ordering/avatars")));

            // 2. GET /api/v1/users/me/avatar (Retrieve)
            mockMvc.perform(get("/api/v1/users/me/avatar")
                            .header("X-User-Id", user.getId())
                            .header("X-User-Role", roleName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl", notNullValue()))
                    .andExpect(jsonPath("$.data.avatarUrl", containsString("food_ordering/avatars")));

            // 3. PUT /api/v1/users/me/avatar (Update URL directly)
            String updatedUrl = "https://res.cloudinary.com/mnzb5t1m/image/upload/v999/food_ordering/avatars/updated_" + roleName.toLowerCase() + ".webp";
            UpdateAvatarRequest updateReq = new UpdateAvatarRequest(updatedUrl);

            mockMvc.perform(put("/api/v1/users/me/avatar")
                            .header("X-User-Id", user.getId())
                            .header("X-User-Role", roleName)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(updatedUrl));

            // Verify DB persistence after PUT
            UserEntity reloadedUser = userRepository.findById(user.getId()).orElseThrow();
            assertEquals(updatedUrl, reloadedUser.getAvatarUrl());

            // 4. DELETE /api/v1/users/me/avatar (Reset/Delete)
            mockMvc.perform(delete("/api/v1/users/me/avatar")
                            .header("X-User-Id", user.getId())
                            .header("X-User-Role", roleName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            // 5. GET /api/v1/users/me/avatar after delete confirms null
            mockMvc.perform(get("/api/v1/users/me/avatar")
                            .header("X-User-Id", user.getId())
                            .header("X-User-Role", roleName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            // Verify DB persistence after DELETE
            reloadedUser = userRepository.findById(user.getId()).orElseThrow();
            assertNull(reloadedUser.getAvatarUrl());
        }
    }

    // =========================================================================
    // 2. ADVERSARIAL STRESS TEST: MIME & Payload Validation and Extension Spoofing
    // =========================================================================
    @Nested
    @DisplayName("Challenge Area 2: MIME, Payload & Spoofing Edge Cases")
    class MimeAndPayloadValidationChallenge {

        @ParameterizedTest(name = "Disallowed MIME/ext {0} ({1}) must be rejected with 400 INVALID_REQUEST")
        @CsvSource({
                "danger.exe, application/x-msdownload",
                "danger.exe, application/octet-stream",
                "document.pdf, application/pdf",
                "payload.sh, text/x-sh",
                "payload.sh, application/x-sh",
                "index.html, text/html",
                "index.htm, text/html",
                "data.json, application/json",
                "script.js, application/javascript",
                "malicious.bat, text/plain",
                "archive.zip, application/zip"
        })
        void rejectDisallowedFiles(String filename, String contentType) throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", filename, contentType, "adversarial-payload-content".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(file)
                            .header("X-User-Id", studentUser.getId())
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("Edge Case 2.1: text/plain disguised with .png filename (fake extension) is rejected")
        void rejectFakeExtension_TextPlainNamedPng() throws Exception {
            MockMultipartFile fakeFile = new MockMultipartFile(
                    "file", "avatar.png", "text/plain", "This is plain text disguise as PNG".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(fakeFile)
                            .header("X-User-Id", studentUser.getId())
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("Edge Case 2.2: .exe extension disguised with image/png MIME is rejected")
        void rejectFakeExtension_ExeNamedWithImagePngMime() throws Exception {
            MockMultipartFile fakeFile = new MockMultipartFile(
                    "file", "malware.exe", MediaType.IMAGE_PNG_VALUE, new byte[]{(byte) 0x4D, (byte) 0x5A, 0x00, 0x00}
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(fakeFile)
                            .header("X-User-Id", studentUser.getId())
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("Edge Case 2.3: .sh extension disguised with image/jpeg MIME is rejected")
        void rejectFakeExtension_ShNamedWithImageJpegMime() throws Exception {
            MockMultipartFile fakeFile = new MockMultipartFile(
                    "file", "attack.sh", MediaType.IMAGE_JPEG_VALUE, "#!/bin/bash\nrm -rf /".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(fakeFile)
                            .header("X-User-Id", studentUser.getId())
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("Edge Case 2.4: .html extension disguised with image/webp MIME is rejected")
        void rejectFakeExtension_HtmlNamedWithImageWebpMime() throws Exception {
            MockMultipartFile fakeFile = new MockMultipartFile(
                    "file", "phishing.html", "image/webp", "<script>stealCookies();</script>".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(fakeFile)
                            .header("X-User-Id", studentUser.getId())
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("Edge Case 2.5: Double extension attack (.png.exe) is rejected")
        void rejectDoubleExtensionAttack() throws Exception {
            MockMultipartFile fakeFile = new MockMultipartFile(
                    "file", "innocent_photo.png.exe", MediaType.IMAGE_PNG_VALUE, "executable-bytes".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(fakeFile)
                            .header("X-User-Id", studentUser.getId())
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        }
    }

    // =========================================================================
    // 3. ADVERSARIAL STRESS TEST: Empty Payload and Payload Boundary Checks
    // =========================================================================
    @Nested
    @DisplayName("Challenge Area 3: Empty and Boundary Payload Stress Tests")
    class PayloadBoundaryChallenge {

        @Test
        @DisplayName("Edge Case 3.1: 0-byte PNG file is rejected with 400 INVALID_REQUEST")
        void rejectEmptyPngFile() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "zero.png", MediaType.IMAGE_PNG_VALUE, new byte[0]
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(emptyFile)
                            .header("X-User-Id", studentUser.getId())
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message", containsString("không được để trống")));
        }

        @Test
        @DisplayName("Edge Case 3.2: 0-byte JPEG file is rejected with 400 INVALID_REQUEST")
        void rejectEmptyJpegFile() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "zero.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(emptyFile)
                            .header("X-User-Id", shipperUser.getId())
                            .header("X-User-Role", "SHIPPER"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("Edge Case 3.3: Direct service invocation with null file throws INVALID_REQUEST")
        void rejectNullFileDirectCall() {
            AppException ex = assertThrows(AppException.class, () -> storageService.uploadAvatar(null, studentUser.getId()));
            assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        }

        @Test
        @DisplayName("Edge Case 3.4: Direct service invocation with empty file throws INVALID_REQUEST")
        void rejectEmptyFileDirectCall() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.webp", "image/webp", new byte[0]
            );
            AppException ex = assertThrows(AppException.class, () -> storageService.uploadAvatar(emptyFile, studentUser.getId()));
            assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        }

        @Test
        @DisplayName("Edge Case 3.5: Oversized file (>10MB) must be rejected with 400 INVALID_REQUEST")
        void testOversizedPayload_Above10MB() throws Exception {
            byte[] oversizedBytes = new byte[11 * 1024 * 1024]; // 11MB
            MockMultipartFile oversizedFile = new MockMultipartFile(
                    "file", "large.png", MediaType.IMAGE_PNG_VALUE, oversizedBytes
            );

            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(oversizedFile)
                            .header("X-User-Id", studentUser.getId())
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        }

        @Test
        @DisplayName("Edge Case 3.6: Direct service invocation with oversized file (>10MB) must throw INVALID_REQUEST")
        void rejectOversizedFileDirectCall() {
            byte[] oversizedBytes = new byte[11 * 1024 * 1024]; // 11MB
            MockMultipartFile oversizedFile = new MockMultipartFile(
                    "file", "large.png", MediaType.IMAGE_PNG_VALUE, oversizedBytes
            );
            AppException ex = assertThrows(AppException.class, () -> storageService.uploadAvatar(oversizedFile, studentUser.getId()));
            assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        }
    }
}
