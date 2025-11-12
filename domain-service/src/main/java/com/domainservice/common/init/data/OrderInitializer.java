package com.domainservice.common.init.data;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.domain.deposit.model.entity.Deposit;
import com.domainservice.domain.deposit.repository.DepositJpaRepository;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.model.entity.OrderItem;
import com.domainservice.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderInitializer {

	private final OrderRepository orderRepository;
	private final DepositJpaRepository depositJpaRepository;

	@Transactional
	public void init() {
		Order order = Order.builder()
			.orderName("test_order")
			.buyerId("test_user_id")
			.build();

		OrderItem orderItem = OrderItem.builder()
			.quantity(1)
			.priceSnapshot(new BigDecimal(1000))
			.build();

		order.addOrderItem(orderItem);
		Order save = orderRepository.save(order);

		Deposit deposit = Deposit.builder()
			.balance(1000000)
			.userId("test_user_id")
			.build();

		depositJpaRepository.save(deposit);

		log.info("orderId = {}", save.getId());

	}

}
