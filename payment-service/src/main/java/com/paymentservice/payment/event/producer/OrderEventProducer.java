package com.paymentservice.payment.event.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.common.event.PaymentCompletedEvent;
import com.common.event.PaymentRefundEvent;
import com.paymentservice.payment.model.enums.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventProducer {

    @Value("${custom.order.topic.command}")
    private String eventTopicName;


    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCompleted(String orderId, String paymentId) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
            orderId,
            PaymentStatus.PAYMENT_COMPLETED.name(),
            paymentId
        ); kafkaTemplate.send(eventTopicName, event).whenComplete((res, e) -> {
            if (e != null) {
                log.error("kafka order status 변경 이벤트 전송 실패 : {}", e.getMessage(), e);
                throw new KafkaException(e.getMessage(), e);
            }
            log.info("kafka order status 변경 이벤트 전송 완료 : {}", res.getRecordMetadata().offset());
        });
    }

    public void publishOrderRefunded(String orderId, String paymentId) {
        PaymentRefundEvent event = new PaymentRefundEvent(
            orderId,
            PaymentStatus.REFUND_AFTER_PAYMENT.name(),
            paymentId
        );
        kafkaTemplate.send(eventTopicName, event).whenComplete((res, e) -> {
            if (e != null) {
                log.error("kafka order status 변경 이벤트 전송 실패 : {}", e.getMessage(), e);
                throw new KafkaException(e.getMessage(), e);
            }
            log.info("kafka order status 변경 이벤트 전송 완료 : {}", res.getRecordMetadata().offset());
        });
    }}
