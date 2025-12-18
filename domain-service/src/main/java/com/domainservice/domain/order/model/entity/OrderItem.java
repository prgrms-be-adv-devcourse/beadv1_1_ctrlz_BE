package com.domainservice.domain.order.model.entity;

import com.common.model.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ordered_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

	@Column(name = "productPost_id", nullable = false)
	private String productPostId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(nullable = false)
	private BigDecimal priceSnapshot;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private OrderItemStatus orderItemStatus;

	public BigDecimal getTotalPrice() {
		if (orderItemStatus == OrderItemStatus.CANCELLED
			|| orderItemStatus == OrderItemStatus.REFUND_AFTER_PAYMENT) {
			return BigDecimal.valueOf(0);
		}
		return priceSnapshot;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public void setOrderItemStatus(OrderItemStatus orderItemStatus) {
		this.orderItemStatus = orderItemStatus;
	}

}
