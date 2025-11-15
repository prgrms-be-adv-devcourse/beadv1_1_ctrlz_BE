package com.settlementservice.common.init.data;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.settlementservice.service.SettlementService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementInitializer {

	private final SettlementService settlementService;

	@PostConstruct
	public void init() {
		log.info("--- 정산 초기 데이터 생성 시작 ---");

		createNormalSettlement("order-item-1", "user-001", BigDecimal.valueOf(30_000));
		createNormalSettlement("order-item-2", "user-002", BigDecimal.valueOf(50_000));

		createNormalSettlement("order-item-404", "user-404", BigDecimal.valueOf(40_000));

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
