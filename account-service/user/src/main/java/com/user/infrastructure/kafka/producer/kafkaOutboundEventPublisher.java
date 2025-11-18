package com.user.infrastructure.kafka.producer;

import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.user.application.port.out.OutboundEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class kafkaOutboundEventPublisher implements OutboundEventPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Override
	public <T> void publish(String topicName, T event) {
		kafkaTemplate.send(topicName, event).whenComplete((res, e) -> {
			if (e != null) {
				log.error("kafka cart 생성 이벤트 전송 실패 : {}", e.getMessage(), e);
				throw new KafkaException(e.getMessage(), e);
			}
			log.info("kafka cart 생성 이벤트 전송 완료 : {}", res.getRecordMetadata().offset());
		});

	}
}
