package com.settlement.job.reader;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.settlement.common.feign.PaymentFeignClient;
import com.settlement.dto.PaymentResponse;
import com.settlement.job.dto.SettlementSourceDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class PaymentSettlementItemReader implements ItemReader<SettlementSourceDto> {

    private final PaymentFeignClient paymentFeignClient;
    private Iterator<SettlementSourceDto> settlementIterator;

    @Value("#{jobParameters['startDate']}")
    private String startDateStr;

    @Value("#{jobParameters['endDate']}")
    private String endDateStr;

    private boolean initialized = false;

    @Override
    public SettlementSourceDto read() {
        if (!initialized) {
            fetchPaymentsAndFlatten();
            initialized = true;
        }

        if (settlementIterator != null && settlementIterator.hasNext()) {
            return settlementIterator.next();
        }
        return null;
    }

    private void fetchPaymentsAndFlatten() {
        LocalDateTime startDate = LocalDateTime.parse(startDateStr);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr);

        log.info("Fetching payments for settlement from {} to {}", startDate, endDate);

        List<PaymentResponse> payments = paymentFeignClient.getPaymentsForSettlement(startDate, endDate).data();
        List<SettlementSourceDto> items = new ArrayList<>();

        if (payments != null) {
            for (PaymentResponse payment : payments) {
                // 이미 정산 가능한 상태만 Payment Service가 준다고 가정하거나,
                // Processor에서 필터링. 여기서는 있는 그대로 넘김.
                items.add(SettlementSourceDto.builder()
                        .userId(payment.userId())
                        .payment(payment)
                        .build());
            }
            this.settlementIterator = items.iterator();
            log.info("Fetched {} payments", payments.size());
        } else {
            this.settlementIterator = null;
            log.warn("Fetched null payment list");
        }
    }
}
