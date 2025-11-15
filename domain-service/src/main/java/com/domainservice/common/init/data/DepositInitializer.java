package com.domainservice.common.init.data;

import java.math.BigDecimal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.domainservice.domain.deposit.service.DepositService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class DepositInitializer {

	private final DepositService depositService;

	@PostConstruct
	public void init() {
		log.info("--- 예치금 초기 데이터 생성 시작 ---");

		// ✅ 예치금 계좌 + 잔액 있는 유저 (정상 정산 확인용)
		initUserDeposit("user-001", BigDecimal.valueOf(100_000));
		initUserDeposit("user-002", BigDecimal.valueOf(50_000));

		// ✅ 계좌는 있지만 0원인 유저 (그래도 정산 들어오면 증가함)
		initUserDeposit("user-003", BigDecimal.ZERO);

		log.info("예치금 계좌 생성하지 않을 테스트 유저: user-404, user-500");

		log.info("--- 예치금 초기 데이터 생성 완료 ---");
	}

	private void initUserDeposit(String userId, BigDecimal amount) {
		try {
			if (amount.compareTo(BigDecimal.ZERO) > 0) {
				depositService.chargeDeposit(userId, amount);
				log.info("예치금 초기화: userId={}, amount={}", userId, amount);
			} else {
				depositService.getDepositByUserId(userId);
				log.info("예치금 계좌만 생성: userId={}, amount=0", userId);
			}
		} catch (Exception e) {
			log.warn("예치금 초기화 실패 userId={}, msg={}", userId, e.getMessage());
		}
	}
}
