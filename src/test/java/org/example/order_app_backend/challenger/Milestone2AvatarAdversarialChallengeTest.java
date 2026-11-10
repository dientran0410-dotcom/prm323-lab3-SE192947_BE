package org.example.order_app_backend.challenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.UpdateAvatarRequest;
import org.example.order_app_backend.dto.request.UpdateUserProfileRequest;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.example.order_app_backend.mapper.UserMapper;
import org.example.order_app_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Milestone 2 - Adversarial Challenger: Avatar Lifecycle, State Transitions & Security Tests")
class Milestone2AvatarAdversarialChallengeTest {

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
        studentUser = getOrCreateUser("student_lifecycle_adv@fpt.edu.vn", "Student Adv Tester", UserRole.STUDENT, "fb_student_adv_01");
        shipperUser = getOrCreateUser("shipper_lifecycle_adv@fpt.edu.vn", "Shipper Adv Tester", UserRole.SHIPPER, "fb_shipper_adv_01");
        storeOwnerUser = getOrCreateUser("owner_lifecycle_adv@fpt.edu.vn", "Owner Adv Tester", UserRole.STORE_OWNER, "fb_owner_adv_01");
        adminUser = getOrCreateUser("admin_lifecycle_adv@fpt.edu.vn", "Admin Adv Tester", UserRole.ADMIN, "fb_admin_adv_01");

        // Clean initial avatar states for determinism
        studentUser.setAvatarUrl(null);
        userRepository.save(studentUser);
        shipperUser.setAvatarUrl(null);
        userRepository.save(shipperUser);
        storeOwnerUser.setAvatarUrl(null);
        userRepository.save(storeOwnerUser);
    }

    private UserEntity getOrCreateUser(String email, String name, UserRole role, String firebaseUid) {
        return userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email(email)
                        .displayName(name)
                        .phone("08" + Math.abs(email.hashCode() % 100000000))
                        .role(role)
                        .firebaseUid(firebaseUid)
                        .ktxBuilding("Khu B - Tòa BA3")
                        .ktxRoom("202")
                        .build())
        );
    }

    @Nested
    @DisplayName("Challenge Area 1: Full Avatar Lifecycle & State Transitions")
    class AvatarLifecycleTests {

        @Test
        @DisplayName("1.1 Complete Lifecycle: Upload -> GET -> PUT -> DELETE -> GET (Null) with DB Verification")
        void testFullAvatarLifecycle_Upload_Get_Put_Delete_GetNull() throws Exception {
            Long userId = studentUser.getId();

            // Initial: Verify GET returns null avatarUrl
            mockMvc.perform(get("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            // Step 1: Upload avatar (POST multipart)
            MockMultipartFile file = new MockMultipartFile(
                    "file", "my_avatar.png", MediaType.IMAGE_PNG_VALUE, "fake-png-content-stream".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(file)
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl", notNullValue()))
                    .andExpect(jsonPath("$.data.avatarUrl", containsString("food_ordering/avatars")))
                    .andReturn();

            String uploadedUrl = com.jayway.jsonpath.JsonPath.read(uploadResult.getResponse().getContentAsString(), "$.data.avatarUrl");
            assertNotNull(uploadedUrl);
            assertTrue(uploadedUrl.contains("user_" + userId));

            // Step 2: GET returns the newly uploaded URL
            mockMvc.perform(get("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(uploadedUrl));

            // Step 3: Direct PUT updates avatarUrl
            String putUrl = "https://res.cloudinary.com/mnzb5t1m/image/upload/v9999/food_ordering/avatars/user_" + userId + "_put.png";
            UpdateAvatarRequest putRequest = new UpdateAvatarRequest(putUrl);

            mockMvc.perform(put("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(putRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(putUrl));

            // Empirically verify DB entity was updated
            UserEntity dbAfterPut = userRepository.findById(userId).orElseThrow();
            assertEquals(putUrl, dbAfterPut.getAvatarUrl());

            // Also verify GET returns the PUT-updated URL
            mockMvc.perform(get("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.avatarUrl").value(putUrl));

            // Step 4: DELETE clears avatar to null
            mockMvc.perform(delete("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            // Empirically verify DB entity avatar is now null
            UserEntity dbAfterDelete = userRepository.findById(userId).orElseThrow();
            assertNull(dbAfterDelete.getAvatarUrl());

            // Step 5: GET returns null avatar
            mockMvc.perform(get("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));
        }

        @Test
        @DisplayName("1.2 Idempotent DELETE: Deleting an already null avatar returns 200 without error")
        void testIdempotentDelete_AlreadyNull_ReturnsOk() throws Exception {
            Long userId = shipperUser.getId();
            shipperUser.setAvatarUrl(null);
            userRepository.save(shipperUser);

            // First delete on null
            mockMvc.perform(delete("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "SHIPPER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            // Second delete on null (idempotency check)
            mockMvc.perform(delete("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "SHIPPER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));
        }

        @Test
        @DisplayName("1.3 Direct PUT with empty or whitespace string resets avatarUrl to null")
        void testPutAvatarUrl_EmptyOrWhitespace_ResetsToNull() throws Exception {
            Long userId = storeOwnerUser.getId();
            storeOwnerUser.setAvatarUrl("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/owner_init.png");
            userRepository.save(storeOwnerUser);

            // PUT with empty string
            UpdateAvatarRequest emptyReq = new UpdateAvatarRequest("");
            mockMvc.perform(put("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STORE_OWNER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            UserEntity dbAfterEmpty = userRepository.findById(userId).orElseThrow();
            assertNull(dbAfterEmpty.getAvatarUrl());

            // Reset back to non-null
            dbAfterEmpty.setAvatarUrl("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/owner_reinit.png");
            userRepository.save(dbAfterEmpty);

            // PUT with whitespace string
            UpdateAvatarRequest spaceReq = new UpdateAvatarRequest("   ");
            mockMvc.perform(put("/api/v1/users/me/avatar")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STORE_OWNER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(spaceReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            UserEntity dbAfterSpace = userRepository.findById(userId).orElseThrow();
            assertNull(dbAfterSpace.getAvatarUrl());
        }
    }

    @Nested
    @DisplayName("Challenge Area 2: UserMapper Reset via PATCH /api/v1/users/me")
    class UserMapperResetTests {

        @Test
        @DisplayName("2.1 Calling PATCH /api/v1/users/me with avatarUrl='' clears avatar to null in DB")
        void testPatchUserMe_EmptyAvatarUrl_ClearsInDatabase() throws Exception {
            Long userId = studentUser.getId();
            studentUser.setAvatarUrl("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/student_pre_patch.png");
            userRepository.save(studentUser);

            // Verify pre-condition
            assertEquals("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/student_pre_patch.png",
                    userRepository.findById(userId).orElseThrow().getAvatarUrl());

            // Execute PATCH with avatarUrl: ""
            UpdateUserProfileRequest patchRequest = UpdateUserProfileRequest.builder()
                    .avatarUrl("")
                    .build();

            mockMvc.perform(patch("/api/v1/users/me")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            // Empirically verify database record
            UserEntity dbUser = userRepository.findById(userId).orElseThrow();
            assertNull(dbUser.getAvatarUrl(), "Database avatarUrl MUST be null after PATCH with empty string");
        }

        @Test
        @DisplayName("2.2 Calling PATCH /api/v1/users/me with whitespace avatarUrl='   ' clears avatar to null in DB")
        void testPatchUserMe_WhitespaceAvatarUrl_ClearsInDatabase() throws Exception {
            Long userId = studentUser.getId();
            studentUser.setAvatarUrl("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/student_ws_test.png");
            userRepository.save(studentUser);

            UpdateUserProfileRequest patchRequest = UpdateUserProfileRequest.builder()
                    .avatarUrl("   ")
                    .build();

            mockMvc.perform(patch("/api/v1/users/me")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            UserEntity dbUser = userRepository.findById(userId).orElseThrow();
            assertNull(dbUser.getAvatarUrl(), "Database avatarUrl MUST be null after PATCH with whitespace string");
        }

        @Test
        @DisplayName("2.3 Calling PATCH /api/v1/users/me omitting avatarUrl preserves existing avatar in DB")
        void testPatchUserMe_NullAvatarUrl_PreservesExistingAvatar() throws Exception {
            Long userId = studentUser.getId();
            String existingUrl = "https://res.cloudinary.com/mnzb5t1m/image/upload/v1/student_keep_me.png";
            studentUser.setAvatarUrl(existingUrl);
            userRepository.save(studentUser);

            // Only update displayName, omitting avatarUrl (avatarUrl = null in DTO)
            UpdateUserProfileRequest patchRequest = UpdateUserProfileRequest.builder()
                    .displayName("New Student Display Name")
                    .build();

            mockMvc.perform(patch("/api/v1/users/me")
                            .header("X-User-Id", userId)
                            .header("X-User-Role", "STUDENT")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.displayName").value("New Student Display Name"))
                    .andExpect(jsonPath("$.data.avatarUrl").value(existingUrl));

            // Empirically verify DB entity still has the original avatarUrl
            UserEntity dbUser = userRepository.findById(userId).orElseThrow();
            assertEquals(existingUrl, dbUser.getAvatarUrl(), "Existing avatarUrl MUST be preserved when omitted from PATCH");
        }
    }

    @Nested
    @DisplayName("Challenge Area 3: Admin Override Capabilities")
    class AdminOverrideTests {

        @Test
        @DisplayName("3.1 Admin can upload avatar for STUDENT and target user sees updated avatar")
        void testAdminUpload_ForStudent_Success() throws Exception {
            Long targetStudentId = studentUser.getId();
            Long adminId = adminUser.getId();

            MockMultipartFile adminFile = new MockMultipartFile(
                    "file", "admin_override.png", MediaType.IMAGE_PNG_VALUE, "admin-override-png-data".getBytes()
            );

            MvcResult result = mockMvc.perform(multipart("/api/v1/admin/users/" + targetStudentId + "/avatar")
                            .file(adminFile)
                            .header("X-User-Id", adminId)
                            .header("X-User-Role", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(targetStudentId))
                    .andExpect(jsonPath("$.data.avatarUrl", notNullValue()))
                    .andExpect(jsonPath("$.data.avatarUrl", containsString("user_" + targetStudentId)))
                    .andReturn();

            String adminSetUrl = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.avatarUrl");

            // Verify in DB
            UserEntity dbStudent = userRepository.findById(targetStudentId).orElseThrow();
            assertEquals(adminSetUrl, dbStudent.getAvatarUrl());

            // Target student calling GET /api/v1/users/me/avatar sees the URL set by Admin
            mockMvc.perform(get("/api/v1/users/me/avatar")
                            .header("X-User-Id", targetStudentId)
                            .header("X-User-Role", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.avatarUrl").value(adminSetUrl));
        }

        @Test
        @DisplayName("3.2 Admin can delete avatar for any user ID and user's avatar becomes null in DB")
        void testAdminDelete_ForShipper_Success() throws Exception {
            Long targetShipperId = shipperUser.getId();
            Long adminId = adminUser.getId();

            shipperUser.setAvatarUrl("https://res.cloudinary.com/mnzb5t1m/image/upload/v1/shipper_to_delete.png");
            userRepository.save(shipperUser);

            mockMvc.perform(delete("/api/v1/admin/users/" + targetShipperId + "/avatar")
                            .header("X-User-Id", adminId)
                            .header("X-User-Role", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(targetShipperId))
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));

            // Verify in DB
            UserEntity dbShipper = userRepository.findById(targetShipperId).orElseThrow();
            assertNull(dbShipper.getAvatarUrl(), "Shipper avatarUrl MUST be null after Admin deletion");

            // Target shipper calling GET /api/v1/users/me/avatar sees null
            mockMvc.perform(get("/api/v1/users/me/avatar")
                            .header("X-User-Id", targetShipperId)
                            .header("X-User-Role", "SHIPPER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.avatarUrl").value(nullValue()));
        }

        @Test
        @DisplayName("3.3 Admin operations on non-existent user ID return 404 Not Found")
        void testAdminOperations_NonExistentUserId_Returns404() throws Exception {
            Long nonExistentId = 999999L;
            Long adminId = adminUser.getId();

            MockMultipartFile file = new MockMultipartFile(
                    "file", "any.png", MediaType.IMAGE_PNG_VALUE, "data".getBytes()
            );

            // POST on non-existent user
            mockMvc.perform(multipart("/api/v1/admin/users/" + nonExistentId + "/avatar")
                            .file(file)
                            .header("X-User-Id", adminId)
                            .header("X-User-Role", "ADMIN"))
                    .andExpect(status().isNotFound());

            // DELETE on non-existent user
            mockMvc.perform(delete("/api/v1/admin/users/" + nonExistentId + "/avatar")
                            .header("X-User-Id", adminId)
                            .header("X-User-Role", "ADMIN"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Challenge Area 4: Security Boundaries & Unauthenticated Requests")
    class SecurityBoundaryTests {

        @Test
        @DisplayName("4.1 Unauthenticated requests to /api/v1/users/me/avatar return 401 Unauthorized")
        void testUnauthenticated_MeAvatar_Returns401() throws Exception {
            // GET without headers
            mockMvc.perform(get("/api/v1/users/me/avatar"))
                    .andExpect(status().isUnauthorized());

            // DELETE without headers
            mockMvc.perform(delete("/api/v1/users/me/avatar"))
                    .andExpect(status().isUnauthorized());

            // PUT without headers
            UpdateAvatarRequest req = new UpdateAvatarRequest("https://example.com/test.png");
            mockMvc.perform(put("/api/v1/users/me/avatar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());

            // POST multipart without headers
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.png", MediaType.IMAGE_PNG_VALUE, "bytes".getBytes()
            );
            mockMvc.perform(multipart("/api/v1/users/me/avatar")
                            .file(file))
                    .andExpect(status().isUnauthorized());
        }
    }
}
