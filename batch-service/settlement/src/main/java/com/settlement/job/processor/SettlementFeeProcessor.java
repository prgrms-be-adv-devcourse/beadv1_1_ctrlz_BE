package com.settlement.job.processor;

import java.math.BigDecimal;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.settlement.job.dto.SettlementModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class SettlementFeeProcessor implements ItemProcessor<SettlementModel, SettlementModel> {

    @Value("${settlement.fee.rate.toss:0.03}") // 카드 수수료율 (기본 3%)
    private BigDecimal tossRate;

    @Value("${settlement.fee.rate.deposit-toss:0.02}") // 카드+예치금 수수료율 (기본 2%)
    private BigDecimal depositTossRate;

    @Value("${settlement.fee.rate.deposit:0.01}") // 예치금 수수료율 (기본 1%)
    private BigDecimal depositRate;

    @Override
    public SettlementModel process(SettlementModel item) throws Exception {
        log.info("정산 수수료 계산 - ID: {}, PayType: {}", item.getId(), item.getPayType());

        item.calculateFee(tossRate, depositTossRate, depositRate);
        item.setStatus("COMPLETED");
        item.setSettledAt(java.time.LocalDateTime.now());

        return item;
    }
}
