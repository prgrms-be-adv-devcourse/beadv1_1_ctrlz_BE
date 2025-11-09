package com.domainservice.domain.deposit.model.entity;

import com.common.exception.CustomException;
import com.common.exception.vo.DepositExceptionCode;
import com.common.model.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deposits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Deposit extends BaseEntity {

	@Column(name = "users_id", nullable = false)
	private String userId; // 사용자 서비스에서 받아오는 ID (FK 아님)

	@Column(name = "balance", nullable = false)
	private int balance; // 잔액

	// 예치금 증가
	public void increaseBalance(int amount) {
		this.balance += amount;
	}

	// 예치금 감소
	public void decreaseBalance(int amount) {
		if (this.balance < amount) {
			throw new CustomException(DepositExceptionCode.INSUFFICIENT_BALANCE.getMessage());
		}
		this.balance -= amount;
	}

	@Override
	protected String getEntitySuffix() {
		return "deposit";
	}
}
