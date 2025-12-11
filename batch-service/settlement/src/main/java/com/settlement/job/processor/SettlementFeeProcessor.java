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

    @Value("${settlement.fee.rate:0.03}") // 수수료율 (기본 3%)
    private BigDecimal feeRate;

    @Override
    public SettlementModel process(SettlementModel item) throws Exception {
        // log.info("Processing settlement fee for item: {}", item.getId());

        item.calculateFee(feeRate);
        item.setStatus("COMPLETED");
        item.setSettledAt(java.time.LocalDateTime.now());

        return item;
    }
}
