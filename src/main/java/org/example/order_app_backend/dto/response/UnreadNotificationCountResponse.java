package org.example.order_app_backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnreadNotificationCountResponse {
    private Long unreadCount;
}
