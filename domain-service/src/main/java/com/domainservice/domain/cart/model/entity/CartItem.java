package com.domainservice.domain.cart.model.entity;

import com.common.model.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@Column(name = "productPost_id", nullable = false)
	private String productPostId;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "selected", nullable = false)
	private boolean selected = true;  // 기본적으로 선택된 상태로 설정

	public void updateQuantity(int quantity) {
		if (quantity > 0) {
			this.quantity = quantity;
			this.updateTime();
		}
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		this.updateTime();
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	// 해당 아이템의 총 가격 계산 (수량 × 상품 가격)
	public int getTotalPrice(int price) {
		return this.quantity * price;
	}

	@Override
	protected String getEntitySuffix() {
		return "cartItem";
	}
}
