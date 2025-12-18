package com.settlement.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.settlement.common.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement extends BaseEntity {

	@Column(name = "order_id", nullable = false)
	private String orderId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount; // 실제 정산 금액 (수수료 제외 전)

	@Column(name = "fee", nullable = true)
	private BigDecimal fee; // 정산 수수료

	@Column(name = "net_amount", nullable = true)
	private BigDecimal netAmount; // 실제 예치금에 들어갈 금액 = amount - fee

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private SettlementStatus settlementStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "pay_type")
	private PayType payType;

	@Column(name = "settled_at")
	private LocalDateTime settledAt;

	public void calculateFee(BigDecimal feeRate) {
		this.fee = this.amount.multiply(feeRate).setScale(0, java.math.RoundingMode.HALF_UP);
		this.netAmount = this.amount.subtract(this.fee);
		this.update(); // BaseEntity.update()
	}

	public void calculateFee(BigDecimal tossRate, BigDecimal depositTossRate, BigDecimal depositRate) {
		BigDecimal feeRate = switch (this.payType) {
			case TOSS -> tossRate;
			case DEPOSIT_TOSS -> depositTossRate;
			case DEPOSIT -> depositRate;
			default -> tossRate;
		};
		this.fee = this.amount.multiply(feeRate).setScale(0, java.math.RoundingMode.HALF_UP);
		this.netAmount = this.amount.subtract(this.fee);
		this.update(); // BaseEntity.update()
	}

	public void complete(LocalDateTime settledAt) {
		this.settlementStatus = SettlementStatus.COMPLETED;
		this.settledAt = settledAt;
		this.update(); // BaseEntity.update()
	}

	public static Settlement create(String orderId, String userId, BigDecimal amount, String payType) {
		Settlement settlement = Settlement.builder()
				.orderId(orderId)
				.userId(userId)
				.amount(amount)
				.payType(PayType.valueOf(payType))
				.fee(BigDecimal.ZERO)
				.netAmount(BigDecimal.ZERO)
				.settlementStatus(SettlementStatus.PENDING)
				.build();

		// JdbcBatchItemWriter 사용 시 @PrePersist가 동작하지 않으므로 수동 초기화
		settlement.id = createEntityId();
		settlement.createdAt = LocalDateTime.now();
		settlement.updatedAt = LocalDateTime.now();
		settlement.deleteStatus = DeleteStatus.N;

		return settlement;
	}
}