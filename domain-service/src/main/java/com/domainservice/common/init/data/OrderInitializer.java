package com.domainservice.common.init.data;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.domain.deposit.model.entity.Deposit;
import com.domainservice.domain.deposit.repository.DepositJpaRepository;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.model.entity.OrderItem;
import com.domainservice.domain.order.model.entity.OrderItemStatus;
import com.domainservice.domain.order.model.entity.OrderStatus;
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
        /**
         * 1. 예치금으로 전체 결제
         * 예치금 10,000 / 주문금액 1,000
         */
        Order order1 = Order.builder()
            .orderName("예치금으로 전체 결제")
            .buyerId("test_user_id_1")
            .orderStatus(OrderStatus.PAYMENT_PENDING)
            .build();

        OrderItem orderItem1 = OrderItem.builder()
            .productPostId("test_product_post_id_1")
            .priceSnapshot(new BigDecimal(1000))
            .orderItemStatus(OrderItemStatus.PAYMENT_PENDING)
            .build();

        order1.addOrderItem(orderItem1);
        Order savedOrder1 = orderRepository.save(order1);

        Deposit deposit1 = Deposit.builder()
            .userId("test_user_id_1")
            .balance(new BigDecimal(10000))
            .build();

        depositJpaRepository.save(deposit1);
        log.info("[1] 예치금 결제 주문 생성 완료 — orderId={}", savedOrder1.getId());

        /**
         * 2. 예치금 + 토스 결제
         * 예치금 5,000 / 주문금액 10,000
         */
        Order order2 = Order.builder()
            .orderName("예치금 + 토스 결제")
            .buyerId("test_user_id_2")
            .orderStatus(OrderStatus.PAYMENT_PENDING)
            .build();

        OrderItem orderItem2 = OrderItem.builder()
            .productPostId("test_product_post_id_2")
            .priceSnapshot(new BigDecimal(10000))
            .orderItemStatus(OrderItemStatus.PAYMENT_PENDING)
            .build();

        order2.addOrderItem(orderItem2);
        Order savedOrder2 = orderRepository.save(order2);

        Deposit deposit2 = Deposit.builder()
            .userId("test_user_id_2")
            .balance(new BigDecimal(5000))
            .build();

        depositJpaRepository.save(deposit2);
        log.info("[2] 예치금 + 토스 결제 주문 생성 완료 — orderId={}", savedOrder2.getId());

        /**
         * 3. 토스로만 결제 (예치금 없음)
         * 예치금 0 / 주문금액 10,000
         */
        Order order3 = Order.builder()
            .orderName("토스로만 결제 1")
            .buyerId("test_user_id_3")
            .orderStatus(OrderStatus.PAYMENT_PENDING)
            .build();

        OrderItem orderItem3 = OrderItem.builder()
            .productPostId("test_product_post_id_3")
            .priceSnapshot(new BigDecimal(10000))
            .orderItemStatus(OrderItemStatus.PAYMENT_PENDING)
            .build();

        order3.addOrderItem(orderItem3);
        Order savedOrder3 = orderRepository.save(order3);

        Deposit deposit3 = Deposit.builder()
            .userId("test_user_id_3")
            .balance(new BigDecimal(0))
            .build();

        depositJpaRepository.save(deposit3);
        log.info("[3] 토스 100% 결제 주문 생성 완료 — orderId={}", savedOrder3.getId());

        /**
         * 4. 토스로만 결제 (예치금 있으나 사용 안함)
         * 예치금 10,000 / 주문금액 20,000
         */
        Order order4 = Order.builder()
            .orderName("토스로만 결제 2")
            .buyerId("test_user_id_4")
            .orderStatus(OrderStatus.PAYMENT_PENDING)
            .build();

        OrderItem orderItem4 = OrderItem.builder()
            .productPostId("test_product_post_id_4")
            .priceSnapshot(new BigDecimal(20000))
            .orderItemStatus(OrderItemStatus.PAYMENT_PENDING)
            .build();

        order4.addOrderItem(orderItem4);
        Order savedOrder4 = orderRepository.save(order4);

        Deposit deposit4 = Deposit.builder()
            .userId("test_user_id_4")
            .balance(new BigDecimal(10000))
            .build();

        depositJpaRepository.save(deposit4);
        log.info("[4] 토스 100% 결제 주문 생성 완료 — orderId={}", savedOrder4.getId());

        log.info("총 4가지 테스트용 주문 및 예치금 데이터 생성 완료!");
    }
}
