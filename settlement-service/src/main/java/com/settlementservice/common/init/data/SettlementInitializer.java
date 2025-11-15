package com.settlementservice.common.init.data;

import java.math.BigDecimal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.settlementservice.service.SettlementService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class SettlementInitializer {

	private final SettlementService settlementService;

	@PostConstruct
	public void init() {
		log.info("--- 정산 초기 데이터 생성 시작 ---");

		// ✅ 정상 케이스 (예치금 있음)
		createNormalSettlement("order-item-1", "user-001", BigDecimal.valueOf(30_000));
		createNormalSettlement("order-item-2", "user-002", BigDecimal.valueOf(50_000));

		// ❌ 실패 케이스 1: 예치금 계좌 없음 → DEPOSIT_NOT_FOUND
		createNormalSettlement("order-item-404", "user-404", BigDecimal.valueOf(40_000));

		// ❌ 실패 케이스 2: 정산 금액 0원 → INVALID_AMOUNT
		createZeroAmountSettlement("order-item-zero", "user-001");

		log.info("--- 정산 초기 데이터 생성 완료 ---");
	}

	private void createNormalSettlement(String orderItemId, String userId, BigDecimal amount) {
		try {
			settlementService.createSettlement(orderItemId, userId, amount);
			log.info("정상 정산 초기 데이터 생성: orderItemId={}, userId={}, amount={}",
				orderItemId, userId, amount);
		} catch (Exception e) {
			log.warn("정상 정산 초기 데이터 생성 실패: orderItemId={}, msg={}",
				orderItemId, e.getMessage());
		}
	}

	private void createZeroAmountSettlement(String orderItemId, String userId) {
		try {
			settlementService.createSettlement(orderItemId, userId, BigDecimal.ZERO);
			log.info("0원 정산 초기 데이터 생성(테스트용): orderItemId={}, userId={}",
				orderItemId, userId);
		} catch (Exception e) {
			log.warn("0원 정산 초기 데이터 생성 실패: orderItemId={}, msg={}",
				orderItemId, e.getMessage());
		}
	}
}
