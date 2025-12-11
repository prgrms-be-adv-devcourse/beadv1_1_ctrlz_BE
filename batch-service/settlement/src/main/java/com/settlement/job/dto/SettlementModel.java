package com.settlement.job.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementModel {
    private String id;
    private String orderItemId;
    private String userId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String payType;
    private String status;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 결제 수단별 차등 수수료 계산
     * 
     * @param tossRate        TOSS(카드) 수수료율
     * @param depositTossRate DEPOSIT_TOSS(카드+예치금) 수수료율
     * @param depositRate     DEPOSIT(예치금) 수수료율
     */
    public void calculateFee(BigDecimal tossRate, BigDecimal depositTossRate, BigDecimal depositRate) {
        BigDecimal feeRate = switch (this.payType) {
            case "TOSS" -> tossRate;
            case "DEPOSIT_TOSS" -> depositTossRate;
            case "DEPOSIT" -> depositRate;
            default -> tossRate; // 기본값은 최고 수수료율
        };
        this.fee = this.amount.multiply(feeRate).setScale(0, RoundingMode.HALF_UP);
        this.netAmount = this.amount.subtract(this.fee);
    }
}
