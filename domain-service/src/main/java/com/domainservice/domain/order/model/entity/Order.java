package com.domainservice.domain.order.model.entity;

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
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

	@Column(name = "user_id", nullable = false)
	private String buyerId;  // 구매자id

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	@Builder.Default
	private List<OrderItem> orderItems = new ArrayList<>();

	@Column(nullable = false, length = 30)
	private OrderStatus orderStatus = OrderStatus.PAYMENT_PENDING;

	public void addOrderItem(OrderItem item) {
		this.orderItems.add(item);
		item.setOrder(this);
	}

	public int getTotalAmount() {
		return orderItems.stream()
			.mapToInt(OrderItem::getTotalPrice)
			.sum();
	}

	@Override
	protected String getEntitySuffix() {
		return "order";
	}
}
