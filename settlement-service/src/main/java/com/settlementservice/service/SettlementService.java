package com.settlementservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settlementservice.domain.entity.Settlement;
import com.settlementservice.domain.entity.SettlementStatus;
import com.settlementservice.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
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

		Settlement settlement = Settlement.builder()
			.orderItemId(orderItemId)
			.userId(userId)
			.amount(amount)
			.fee(fee)
			.netAmount(netAmount)
			.settlementStatus(SettlementStatus.PENDING)
			.build();

		return settlementRepository.save(settlement);
	}
}