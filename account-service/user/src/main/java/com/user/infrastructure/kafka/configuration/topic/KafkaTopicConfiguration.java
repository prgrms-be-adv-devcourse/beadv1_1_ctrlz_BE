package com.user.infrastructure.kafka.configuration.topic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
public class KafkaTopicConfiguration {

	@Value("${custom.cart.topic.command}")
	private String cartCommandTopic;

	@Value("${custom.config.topic-partitions}")
	private int topicPartitions;

	@Value("${custom.config.topic-replications}")
	private int topicReplications;

	@Bean
	public NewTopic createCartsCommandTopic() {
		return TopicBuilder.name(cartCommandTopic)
			.partitions(topicPartitions)
			.replicas(topicReplications)
			.build();
	}
}
