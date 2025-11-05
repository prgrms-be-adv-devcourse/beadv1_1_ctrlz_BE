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

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "product_post_id", nullable = false)
	// private ProductPost productPost;
	//
	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "seller_id", nullable = false)
	// private User seller;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "price", nullable = false)
	private Integer price;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(name = "total_amount", nullable = false)
	private Integer totalAmount;

	/**
	 * 총 금액 계산 메서드
	 */
	public void calculateTotalAmount() {
		this.totalAmount = this.price * this.quantity;
	}

	@Override
	protected String getEntitySuffix() {
		return "OrderItems";
	}
}
