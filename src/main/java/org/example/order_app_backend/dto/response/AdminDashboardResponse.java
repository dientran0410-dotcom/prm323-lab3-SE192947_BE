package org.example.order_app_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private Double totalRevenue;
    private Long totalOrders;
    private Long activeOrders;
    private Long pendingDisputes;
    private Long totalUsers;
    private Double disputeResolutionRate;
}

