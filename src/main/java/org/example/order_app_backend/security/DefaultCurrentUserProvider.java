package org.example.order_app_backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class DefaultCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<CurrentUser> getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return Optional.empty();
        }

        HttpServletRequest request = attributes.getRequest();
        String userIdHeader = request.getHeader("X-User-Id");
        String firebaseUidHeader = request.getHeader("X-Firebase-Uid");
        String emailHeader = request.getHeader("X-User-Email");
        String roleHeader = request.getHeader("X-User-Role");
        String nameHeader = request.getHeader("X-User-Name");

        if (userIdHeader != null || firebaseUidHeader != null) {
            Long userId = null;
            if (userIdHeader != null) {
                try {
                    userId = Long.parseLong(userIdHeader);
                } catch (NumberFormatException ignored) {}
            }

            return Optional.of(CurrentUser.builder()
                    .id(userId != null ? userId : 1L)
                    .firebaseUid(firebaseUidHeader != null ? firebaseUidHeader : "mock_firebase_uid")
                    .email(emailHeader != null ? emailHeader : "student@fpt.edu.vn")
                    .displayName(nameHeader != null ? nameHeader : "Sinh viên FPT")
                    .role(roleHeader != null ? roleHeader : "STUDENT")
                    .build());
        }

        return Optional.empty();
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().map(CurrentUser::getId).orElse(null);
    }

    @Override
    public String getCurrentUserFirebaseUid() {
        return getCurrentUser().map(CurrentUser::getFirebaseUid).orElse(null);
    }

    @Override
    public boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    @Override
    public boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> role.equalsIgnoreCase(user.getRole()))
                .orElse(false);
    }
}

