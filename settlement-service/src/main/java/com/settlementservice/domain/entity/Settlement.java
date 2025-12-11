package com.settlementservice.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.common.model.persistence.BaseEntity;

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

	@Column(name = "order_item_id", nullable = false)
	private String orderItemId;

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
	}

	public static Settlement create(String orderItemId, String userId, BigDecimal amount) {
		return Settlement.builder()
				.orderItemId(orderItemId)
				.userId(userId)
				.amount(amount)
				.fee(BigDecimal.ZERO)
				.netAmount(BigDecimal.ZERO)
				.settlementStatus(SettlementStatus.PENDING)
				.build();
	}
}