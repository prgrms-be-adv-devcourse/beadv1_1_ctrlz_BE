package com.domainservice.domain.deposit.service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.common.event.SettlementCompletedEvent;
import com.common.event.SettlementFailedEvent;
import com.common.event.SettlementReadyEvent;
import com.common.exception.CustomException;
import com.domainservice.domain.deposit.service.DepositService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementReadyEventConsumer {

	private final DepositService depositSettlementService;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@KafkaListener(
		topics = "settlement-ready",
		groupId = "deposit-settlement-group"
	)
	public void consume(SettlementReadyEvent event) {

		try {
			// 예치금 처리
			depositSettlementService.processSettlement(event);

			// 처리 성공 → 정산 서비스에 완료 이벤트 발행
			SettlementCompletedEvent completedEvent = new SettlementCompletedEvent(
				event.settlementId(),
				event.userId()
			);

			kafkaTemplate.send(
				"settlement-completed",
				event.settlementId(),
				completedEvent
			);

		} catch (CustomException e) {
			log.warn("정산 예치금 처리 비즈니스 실패 settlementId={}, reason={}",
				event.settlementId(), e.getMessage());

			depositSettlementService.markSettlementFailed(event, e.getMessage());

			SettlementFailedEvent failedEvent = new SettlementFailedEvent(
				event.settlementId(),
				event.userId(),
				e.getMessage()
			);

			kafkaTemplate.send(
				"settlement-failed",
				event.settlementId(),
				failedEvent
			);

		} catch (Exception e) {
			// 재처리 대상
			// TODO  시스템 오류 → 지금은 재시도 대상 (throw e 유지)
			log.error("정산 예치금 처리 중 시스템 오류 settlementId={}", event.settlementId(), e);
			throw e;
		}
	}
}
