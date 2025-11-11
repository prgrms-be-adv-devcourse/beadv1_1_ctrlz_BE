package com.domainservice.domain.order.model.entity;

import com.common.model.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Ordered_items")
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

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private int priceSnapshot;

	public int getTotalPrice() {
		return quantity * priceSnapshot;
	}

	void setOrder(Order order) {
		this.order = order;
	}

	@Override
	protected String getEntitySuffix() {
		return "OrderItems";
	}
}
