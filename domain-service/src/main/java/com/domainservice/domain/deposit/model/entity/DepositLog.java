package com.domainservice.domain.deposit.model.entity;

import java.math.BigDecimal;

import com.common.model.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deposit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DepositLog extends BaseEntity {

	@Column(name = "users_id", nullable = false)
	private String userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deposit_id", nullable = false)
	private Deposit deposit; // 예치금 엔티티 참조 (같은 서비스 내 FK)

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false)
	private TransactionType transactionType; // 충전, 구매, 출금, 환불

	@Column(name = "amount", nullable = false)
	private BigDecimal amount; // 거래 금액

	@Column(name = "before_balance", nullable = false)
	private BigDecimal beforeBalance; // 이전 잔액

	@Column(name = "after_balance", nullable = false)
	private BigDecimal afterBalance; // 이후 잔액

	// 정산 이벤트와 매핑
	@Column(name = "reference_id")
	private String referenceId;

	@Override
	protected String getEntitySuffix() {
		return "depositlog";
	}

	public static DepositLog create(String userId, Deposit deposit, TransactionType type, BigDecimal amount,
		BigDecimal before,
		BigDecimal after) {
		return DepositLog.builder()
			.userId(userId)
			.deposit(deposit)
			.transactionType(type)
			.amount(amount)
			.beforeBalance(before)
			.afterBalance(after)
			.build();
	}

	public static DepositLog createBySettlementReady(String userId, Deposit deposit,
		TransactionType type, BigDecimal amount,
		BigDecimal before, BigDecimal after,
		String referenceId) {
		return DepositLog.builder()
			.userId(userId)
			.deposit(deposit)
			.transactionType(type)
			.amount(amount)
			.beforeBalance(before)
			.afterBalance(after)
			.referenceId(referenceId)
			.build();
	}
}
