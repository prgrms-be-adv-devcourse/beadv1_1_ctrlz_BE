package com.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.settlement.domain.entity.Settlement;
import com.settlement.domain.entity.SettlementStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementDto {
    private String id;
    private String orderItemId;
    private String userId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private SettlementStatus status;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SettlementDto from(Settlement settlement) {
        return SettlementDto.builder()
                .id(settlement.getId())
                .orderItemId(settlement.getOrderItemId())
                .userId(settlement.getUserId())
                .amount(settlement.getAmount())
                .fee(settlement.getFee())
                .netAmount(settlement.getNetAmount())
                .status(settlement.getSettlementStatus())
                .settledAt(settlement.getSettledAt())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }
}
