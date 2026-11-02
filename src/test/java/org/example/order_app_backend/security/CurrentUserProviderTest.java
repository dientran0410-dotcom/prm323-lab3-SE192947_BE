package org.example.order_app_backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserProviderTest {

    private final DefaultCurrentUserProvider currentUserProvider = new DefaultCurrentUserProvider();

    @Test
    @DisplayName("CurrentUserProvider: Trích xuất đúng thông tin người dùng từ HTTP Headers")
    void getCurrentUser_WithHeaders_ShouldReturnUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "123");
        request.addHeader("X-Firebase-Uid", "fb_student_999");
        request.addHeader("X-User-Email", "student@fpt.edu.vn");
        request.addHeader("X-User-Role", "STUDENT");
        request.addHeader("X-User-Name", "Nguyễn Văn Sinh Viên");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            Optional<CurrentUser> currentUser = currentUserProvider.getCurrentUser();
            assertTrue(currentUser.isPresent());
            assertEquals(123L, currentUser.get().getId());
            assertEquals("fb_student_999", currentUser.get().getFirebaseUid());
            assertEquals("student@fpt.edu.vn", currentUser.get().getEmail());
            assertEquals("STUDENT", currentUser.get().getRole());
            assertTrue(currentUserProvider.isAuthenticated());
            assertTrue(currentUserProvider.hasRole("STUDENT"));
            assertFalse(currentUserProvider.hasRole("ADMIN"));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("CurrentUserProvider: Khi không có header định danh, trả về Empty và unauthenticated")
    void getCurrentUser_NoHeaders_ShouldReturnEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            Optional<CurrentUser> currentUser = currentUserProvider.getCurrentUser();
            assertTrue(currentUser.isEmpty());
            assertNull(currentUserProvider.getCurrentUserId());
            assertFalse(currentUserProvider.isAuthenticated());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}

