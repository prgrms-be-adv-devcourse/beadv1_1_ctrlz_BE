package com.user.infrastructure.kafka.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * 실제 kafka는 운영 환경에서 동작합니다.
 * 로컬 실행 시 profile을 바꿀 수 있습니다.
 */
@Configuration
public class KafkaConfiguration {

	@Value("${custom.cart.topic.command}")
	private String cartCommandTopic;

	@Value("${custom.config.topic-partitions}")
	private int topicPartitions;
	@Value("${custom.config.topic-replications}")
	private int topicReplications;

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(
		ProducerFactory<String, Object> producerFactory
	) {
		return new KafkaTemplate<>(producerFactory);
	}
}
