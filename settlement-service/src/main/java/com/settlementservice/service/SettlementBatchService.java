package com.settlementservice.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.settlementservice.domain.entity.Settlement;
import com.settlementservice.domain.entity.SettlementStatus;
import com.settlementservice.repository.SettlementRepository;
import com.settlementservice.service.producer.SettlementReadyEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementBatchService {

	private final SettlementRepository settlementRepository;
	private final SettlementReadyEventProducer depositSettlementProducer; // Kafka producer

	// 실행후 한번만 작동
	// @Scheduled(initialDelay = 60_000, fixedDelay = Long.MAX_VALUE)
	// @Scheduled(cron = "0 */1 * * * *")
	// @Scheduled(cron = "0 0 3 15 * *") 매달 15일 새벽 3시
	public void runSettlementBatch() {
		log.info("SettlementBatchService run");
		while (true) {
			List<Settlement> settlements =
				settlementRepository.findTop100BySettlementStatusOrderByCreatedAtAsc(SettlementStatus.PENDING);
			if (settlements.isEmpty())
				break;

			for (Settlement settlement : settlements) {
				try {
					// 정산 건별로 처리 시작
					// 상태 확인
					if (settlement.getSettlementStatus() != SettlementStatus.PENDING) {
						continue;
					}

					// 예치금 서비스로 카프카 전송
					depositSettlementProducer.sendSettlementReadyEvent(
						settlement.getUserId(),
						settlement.getNetAmount(),
						settlement.getId()
					);

					settlement.markReady();
					settlementRepository.save(settlement);

				} catch (Exception e) {
					log.error("정산 실패 settlementId={}", settlement.getId(), e);
					// 실패 상태 업데이트
					settlement.markFailed();
					settlementRepository.save(settlement);
				}
			}
		}
	}

	// 실패한 정산 재시도 로직
	@Scheduled(cron = "0 0 * * * *") // 매 시간 재시도
	public void retryFailedSettlements() {

		List<Settlement> failedList =
			settlementRepository.findTop100BySettlementStatusOrderByCreatedAtAsc(SettlementStatus.FAILED);

		for (Settlement settlement : failedList) {
			try {
				// 1) FAILED → PENDING
				settlement.markPendingAgain();
				settlementRepository.save(settlement);
				// 2) 정산 1건 다시 처리
				processSingleSettlementSimple(settlement);

			} catch (Exception e) {
				log.error("정산 재시도 실패 settlementId={}", settlement.getId(), e);
			}
		}
	}

	public void processSingleSettlementSimple(Settlement settlement) {

		try {
			if (settlement.getSettlementStatus() != SettlementStatus.PENDING) {
				return;
			}

			// 예치금 서비스로 Kafka 메시지 발행
			depositSettlementProducer.sendSettlementReadyEvent(
				settlement.getUserId(),
				settlement.getNetAmount(),
				settlement.getId()
			);

			settlement.markReady();
			settlementRepository.save(settlement);

		} catch (Exception e) {

			// 실패하면 다시 FAILED
			settlement.markFailed();
			settlementRepository.save(settlement);

			throw e;
		}
	}
}
