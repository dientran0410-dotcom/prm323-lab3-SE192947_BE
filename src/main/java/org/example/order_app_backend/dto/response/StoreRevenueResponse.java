package org.example.order_app_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRevenueResponse {
    private BigDecimal totalRevenue;
    private BigDecimal codAmount;
    private BigDecimal onlineAmount;
    private Integer totalOrders;
    private Integer completedOrders;
    private String shiftName;
    private List<OwnerOrderSummaryResponse> orderDetails;
}
