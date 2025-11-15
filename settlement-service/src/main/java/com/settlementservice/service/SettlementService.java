package com.settlementservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.event.SettlementCompletedEvent;
import com.common.event.SettlementFailedEvent;
import com.common.exception.CustomException;
import com.settlementservice.domain.entity.Settlement;
import com.settlementservice.domain.entity.SettlementStatus;
import com.settlementservice.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SettlementService {
	private final SettlementRepository settlementRepository;

	// 예시: 수수료 10% 계산
	public static final BigDecimal FEE_RATE = new BigDecimal("0.1");

	/**
	 * 구매확정된 주문에 대해 정산 데이터 생성
	 */
	public Settlement createSettlement(String orderItemId, String userId, BigDecimal amount) {

		BigDecimal fee = amount.multiply(FEE_RATE).setScale(0, RoundingMode.HALF_UP);
		BigDecimal netAmount = amount.subtract(fee);

		Settlement settlement = Settlement.create(orderItemId, userId, amount, fee, netAmount);

		return settlementRepository.save(settlement);
	}

	/**
	 * 예치금에서 정산 완료 이벤트 수신 시 상태 업데이트
	 */
	public void handleSettlementCompleted(SettlementCompletedEvent event) {
		Settlement settlement = settlementRepository.findById(event.settlementId())
			.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. settlementId=" + event.settlementId()));

		// 멱등
		if (settlement.getSettlementStatus() == SettlementStatus.COMPLETED) {
			log.info("이미 완료된 정산입니다. settlementId={}", event.settlementId());
			return;
		}

		// 이미 FAILED 상태면 정책에 따라 무시
		if (settlement.getSettlementStatus() == SettlementStatus.FAILED) {
			log.warn("FAILED 상태의 정산에 COMPLETED 이벤트 수신 settlementId={}", event.settlementId());
			return;
		}

		settlement.markCompleted();
		log.info("정산 완료 처리 settlementId={}", event.settlementId());
	}

	/**
	 * 예치금에서 정산 실패 이벤트 수신 시 상태 업데이트
	 */
	public void handleSettlementFailed(SettlementFailedEvent event) {
		Settlement settlement = settlementRepository.findById(event.settlementId())
			.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. settlementId=" + event.settlementId()));

		// 이미 COMPLETED면 실패 이벤트는 무시
		if (settlement.getSettlementStatus() == SettlementStatus.COMPLETED) {
			log.warn("완료된 정산에 FAILED 이벤트 수신, 무시 settlementId={}", event.settlementId());
			return;
		}

		settlement.markFailed();
		log.warn("정산 실패 처리 settlementId={}, reason={}", event.settlementId(), event.reason());
	}
}