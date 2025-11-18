package com.domainservice.domain.order.model.entity;

import static com.domainservice.domain.order.model.entity.OrderStatus.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.payment.model.entity.PaymentEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

	@Column(nullable = false)
	private String orderName;

	@Column(nullable = false, length = 30)
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private PaymentEntity payment;

	public void addOrderItem(OrderItem item) {
		this.orderItems.add(item);
		item.setOrder(this);
	}

	public BigDecimal getTotalAmount() {
		return orderItems.stream()
			.map(OrderItem::getTotalPrice)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void orderConfirmed() {
		orderStatus = PURCHASE_CONFIRMED;
		orderItems.forEach(item -> item.setOrderItemStatus(OrderItemStatus.PURCHASE_CONFIRMED));
	}

	public void orderCanceled() {
		orderStatus = CANCELLED;
		orderItems.forEach(item -> item.setOrderItemStatus(OrderItemStatus.CANCELLED));
	}

	public void orderRefundedAfterPayment() {
		orderStatus = REFUND_AFTER_PAYMENT;
		orderItems.forEach(item -> item.setOrderItemStatus(OrderItemStatus.REFUND_AFTER_PAYMENT));
	}

	@Override
	protected String getEntitySuffix() {
		return "order";
	}

	public void setPayment(PaymentEntity paymentEntity) {
		this.payment = paymentEntity;
	}
}
