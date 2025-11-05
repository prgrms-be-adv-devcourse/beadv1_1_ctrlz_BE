package com.domainservice.domain.order.model.entity;

import java.time.LocalDateTime;

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
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "user_id", nullable = false)
	// // private UserEntity user;  // 사용자 (FK)

	@Column(nullable = false)
	private LocalDateTime orderedAt;  // 주문 일시

	@Column(nullable = false)
	private Integer totalAmount;  // 총 결제 금액

	@Column(nullable = false, length = 30)
	private OrderStatus orderStatus = OrderStatus.PAYMENT_PENDING;

	@Override
	protected String getEntitySuffix() {
		return "order";
	}
}
