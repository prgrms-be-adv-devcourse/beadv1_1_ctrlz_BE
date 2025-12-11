package com.settlement.job.dto;

import com.settlement.common.model.payment.PaymentResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettlementSourceDto {
    private String userId; // PaymentResponse에도 있지만, Reader에서 세팅하기 쉬우므로 유지 혹은 제거 고려.
    // PaymentResponse에 userId가 있으므로 사실 중복이나, 명시적으로 유지해도 됨.
    // 하지만 Plan대로 PaymentResponse로 교체.

    private PaymentResponse payment;
}
