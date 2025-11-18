package com.domainservice.domain.cart.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.common.model.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

	@Column(name = "user_id", nullable = false)
	private String userId;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartItem> cartItems = new ArrayList<>();

	// 장바구니 아이템 추가
	public void addCartItem(CartItem cartItem) {
		cartItems.add(cartItem);
		cartItem.setCart(this);
	}

	// 장바구니 아이템 삭제
	public void removeCartItem(CartItem cartItem) {
		cartItems.remove(cartItem);
		cartItem.setCart(null);
	}

	@Override
	protected String getEntitySuffix() {
		return "cart";
	}
}
