package org.example.order_app_backend.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {
    private Long id;
    private String firebaseUid;
    private String email;
    private String displayName;
    private String role; // STUDENT, STORE_OWNER, SHIPPER, ADMIN
}
