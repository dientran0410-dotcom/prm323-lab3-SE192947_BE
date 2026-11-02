package org.example.order_app_backend.security;

import java.util.Optional;

public interface CurrentUserProvider {

    Optional<CurrentUser> getCurrentUser();

    Long getCurrentUserId();

    String getCurrentUserFirebaseUid();

    boolean isAuthenticated();

    boolean hasRole(String role);
}
