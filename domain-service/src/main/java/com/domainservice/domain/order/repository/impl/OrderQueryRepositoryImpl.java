package com.domainservice.domain.order.repository.impl;

import java.util.Optional;

import com.domainservice.domain.order.model.dto.OrderedAt;
import com.domainservice.domain.order.model.entity.OrderStatus;
import com.domainservice.domain.order.repository.OrderQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static com.domainservice.domain.order.model.entity.QOrder.order;
import static com.domainservice.domain.order.model.entity.QOrderItem.orderItem;

@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

	private final JPAQueryFactory queryFactory;


	@Override
	public Optional<OrderedAt> findOrderedAtByBuyerIdAndProductPostId(String productPostId, String userId) {
		OrderedAt result = queryFactory
			.select(
				Projections.constructor(
					OrderedAt.class,
					order.updatedAt
				)
			)
			.from(order)
			.join(orderItem).on(orderItem.order.eq(order))
			.where(
				order.buyerId.eq(userId),
				order.orderStatus.eq(OrderStatus.PURCHASE_CONFIRMED),
				orderItem.productPostId.eq(productPostId)
			)
			.fetchOne();

		return Optional.ofNullable(result);
	}

}
