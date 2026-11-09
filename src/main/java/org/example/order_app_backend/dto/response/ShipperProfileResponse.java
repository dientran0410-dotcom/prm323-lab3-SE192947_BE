package org.example.order_app_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipperProfileResponse {
    private Long shipperId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String avatarUrl;
    private BigDecimal shipperRatingAvg;
    private Integer ratingCount;
    private Integer totalTrips;
}
