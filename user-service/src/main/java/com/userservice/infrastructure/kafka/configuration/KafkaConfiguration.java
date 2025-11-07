package com.userservice.infrastructure.kafka.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * 실제 kafka는 운영 환경에서 동작합니다.
 * 로컬 실행 시 profile을 바꿀 수 있습니다.
 */
@Profile("prod")
@Configuration
public class KafkaConfiguration {

	@Value("${custom.cart.topic.command}")
	private String cartTopicCommand;

	@Value("${custom.deposit.topic.command}")
	private String depositTopicCommand;

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

	@Bean
	public NewTopic createCartsCommandTopic() {
		return TopicBuilder.name(cartTopicCommand)
			.partitions(topicPartitions)
			.replicas(topicReplications)
			.build();
	}

	@Bean
	public NewTopic createDepositCommandTopic() {
		return TopicBuilder.name(depositTopicCommand)
			.partitions(topicPartitions)
			.replicas(topicReplications)
			.build();
	}
}
