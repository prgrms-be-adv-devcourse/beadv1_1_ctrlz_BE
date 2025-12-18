package com.paymentservice.common.init.data;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.deposit.service.DepositService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DepositInitializer {

	private final DepositService depositService;

	public void init() {
		log.info("--- 예치금 초기화 시작 ---");

		List<String> userIds = List.of("user-001", "user-002", "user-003", "user-004", "user-005");

		for (String userId : userIds) {

			try {
				Deposit deposit = depositService.createDeposit(userId);
				log.info("[{}] 예치금 생성 완료: depositId={}", userId, deposit.getId());
			} catch (Exception e) {
				log.warn("[{}] 예치금 생성 실패: {}", userId, e.getMessage());
			}
		}

		log.info("--- 예치금 초기화 완료 ---");
	}
}
