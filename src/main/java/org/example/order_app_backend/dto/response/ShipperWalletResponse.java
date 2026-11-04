package org.example.order_app_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipperWalletResponse {
    private BigDecimal walletBalance;
    private BigDecimal pendingEarnings;
    private BigDecimal completedEarnings;
    private Integer completedTrips;
    private BigDecimal codCollectedToday;
    private List<WalletTransactionItem> recentTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletTransactionItem {
        private String transactionId;
        private String type; // CONG_THU_LAO, RUT_TIEN, NOP_COD
        private String title;
        private BigDecimal amount;
        private LocalDateTime createdAt;
        private String status; // SUCCESS, PENDING, FAILED
    }
}
