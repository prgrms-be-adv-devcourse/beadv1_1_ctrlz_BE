package com.settlement.job.processor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.settlement.dto.PaymentResponse;
import com.settlement.domain.entity.Settlement;
import com.settlement.job.dto.SettlementSourceDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class SettlementCreateProcessor implements ItemProcessor<SettlementSourceDto, Settlement> {

    @Override
    public Settlement process(SettlementSourceDto item) throws Exception {
        PaymentResponse payment = item.getPayment();

        // 결제 완료 상태가 아니면 필터링
        if (!"SUCCESS".equals(payment.status())) {
            return null;
        }

        return Settlement.create(
                payment.orderId(),
                item.getUserId(),
                payment.amount(),
                payment.payType());
    }
}
