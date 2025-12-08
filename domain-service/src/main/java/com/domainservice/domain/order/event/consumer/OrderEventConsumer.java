package com.domainservice.domain.order.event.consumer;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.common.event.PaymentCompletedEvent;
import com.common.event.PaymentRefundEvent;
import com.domainservice.domain.order.model.entity.OrderStatus;
import com.domainservice.domain.order.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = "${custom.order.topic.command}",
    groupId = "${spring.kafka.consumer.group-id}"
)
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaHandler
    public void handleCompleted(@Payload PaymentCompletedEvent event) {
        if (event.orderStatus().equals(OrderStatus.PAYMENT_COMPLETED.name()) ) {

            orderService.updateStatus(event.orderId(), OrderStatus.PAYMENT_COMPLETED, event.paymentId());

            log.info("Order {} updated to PAYMENT_COMPLETED", event.orderId());
        }
    }

    @KafkaHandler
    public void handleRefunded(PaymentRefundEvent event) {
        if (event.orderStatus().equals(OrderStatus.REFUND_AFTER_PAYMENT.name())) {

            orderService.updateStatus(event.orderId(), OrderStatus.REFUND_AFTER_PAYMENT, event.paymentId());

            log.info("Order {} updated to REFUND_AFTER_PAYMENT", event.orderId());
        }
    }
}
