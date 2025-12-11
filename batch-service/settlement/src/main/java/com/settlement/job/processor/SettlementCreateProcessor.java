package com.settlement.job.processor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.settlement.dto.PaymentResponse;
import com.settlement.domain.entity.SettlementStatus;
import com.settlement.job.dto.SettlementModel;
import com.settlement.job.dto.SettlementSourceDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class SettlementCreateProcessor implements ItemProcessor<SettlementSourceDto, SettlementModel> {

    @Override
    public SettlementModel process(SettlementSourceDto item) throws Exception {
        PaymentResponse payment = item.getPayment();

        // 결제 완료 상태가 아니면 필터링 (이미 Payment Service에서 걸러줄 수도 있지만 안전장치)
        if (!"PAID".equals(payment.status())) { // PaymentStatus.PAID.name() assuming string "PAID"
            return null;
        }

        return SettlementModel.builder()
                .id(UUID.randomUUID().toString())
                .orderItemId(payment.orderItemId())
                .userId(item.getUserId())
                .amount(payment.amount())
                .payType(payment.payType())
                .status(SettlementStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
