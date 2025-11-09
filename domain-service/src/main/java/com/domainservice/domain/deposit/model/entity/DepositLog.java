package com.domainservice.domain.deposit.model.entity;

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
	private int amount; // 거래 금액

	@Column(name = "before_balance", nullable = false)
	private int beforeBalance; // 이전 잔액

	@Column(name = "after_balance", nullable = false)
	private int afterBalance; // 이후 잔액

	@Override
	protected String getEntitySuffix() {
		return "depositlog";
	}

	public static DepositLog create(String userId, Deposit deposit, TransactionType type, int amount, int before,
		int after) {
		return DepositLog.builder()
			.userId(userId)
			.deposit(deposit)
			.transactionType(type)
			.amount(amount)
			.beforeBalance(before)
			.afterBalance(after)
			.build();
	}
}
