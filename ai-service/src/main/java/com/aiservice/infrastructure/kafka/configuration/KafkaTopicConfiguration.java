package com.aiservice.infrastructure.kafka.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

	@Value("${custom.product-post.topic.event}")
	private String productPostTopic;

	@Value("${custom.config.topic-partitions}")
	private int topicPartitions;

	@Value("${custom.config.topic-replications}")
	private int topicReplications;

	@Bean
	public NewTopic createProductPostTopic() {
		return TopicBuilder.name(productPostTopic)
			.partitions(topicPartitions)
			.replicas(topicReplications)
			.build();
	}

	/*
	 * DLT 생성
	 */
	@Bean
	public NewTopic createProductPostDLT() {
		return TopicBuilder.name(productPostTopic + ".DLT")
			.partitions(topicPartitions)
			.replicas(topicReplications)
			.build();
	}
}
