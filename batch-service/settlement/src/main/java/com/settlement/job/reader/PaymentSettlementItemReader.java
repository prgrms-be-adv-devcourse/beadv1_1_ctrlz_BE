package com.settlement.job.reader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

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
@StepScope
@Component
@RequiredArgsConstructor
public class PaymentSettlementItemReader implements ItemReader<SettlementSourceDto> {

	private final PaymentFeignClient paymentFeignClient;

	@Value("#{jobParameters['startDate']}")
	private String startDateStr;

	@Value("#{jobParameters['endDate']}")
	private String endDateStr;

	private List<SettlementSourceDto> settlementItems;
	private int currentIndex = 0;

	@Override
	public SettlementSourceDto read() {
		if (settlementItems == null) {
			initializePayments();
		}

		if (currentIndex < settlementItems.size()) {
			return settlementItems.get(currentIndex++);
		}

		return null;
	}

	private void initializePayments() {
		try {
			LocalDateTime startDate = LocalDateTime.parse(startDateStr, DateTimeFormatter.ISO_DATE_TIME);
			LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_DATE_TIME);

			log.info("정산 날짜 from {} to {}", startDate, endDate);

			List<PaymentResponse> payments = fetchPayments(startDate, endDate);
			this.settlementItems = convertToSettlementItems(payments);

			log.info("payment 조회 완료 {} payments", settlementItems.size());
		} catch (Exception e) {
			log.error("payment 조회에 실패했습니다.", e);
			this.settlementItems = Collections.emptyList();
		}
	}

	private List<PaymentResponse> fetchPayments(LocalDateTime startDate, LocalDateTime endDate) {
		List<PaymentResponse> payments = paymentFeignClient
			.getPaymentsForSettlement(startDate, endDate)
			.data();

		return payments != null ? payments : Collections.emptyList();
	}

	private List<SettlementSourceDto> convertToSettlementItems(List<PaymentResponse> payments) {
		return payments.stream()
			.map(payment -> SettlementSourceDto.builder()
				.userId(payment.userId())
				.payment(payment)
				.build())
			.toList();
	}
}