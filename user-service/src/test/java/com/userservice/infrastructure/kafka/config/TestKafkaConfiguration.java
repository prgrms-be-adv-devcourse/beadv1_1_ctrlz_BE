package com.userservice.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Profile("test")
@Configuration
public class TestKafkaConfiguration {

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
