package com.settlement.job.dto;

import java.math.BigDecimal;
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
    private String status;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void calculateFee(BigDecimal feeRate) {
        this.fee = this.amount.multiply(feeRate).setScale(0, java.math.RoundingMode.HALF_UP);
        this.netAmount = this.amount.subtract(this.fee);
    }
}
