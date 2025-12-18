// KafkaProductPostConfiguration.java
package com.domainservice.domain.post.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaProductPostConfiguration {

	@Value("${custom.product-post.topic.event}")
	private String productPostEventTopicName;

	@Value("${custom.config.topic-partitions}")
	private int topicPartitions;

	@Value("${custom.config.topic-replications}")
	private int topicReplicationFactors;

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> factory) {
		return new KafkaTemplate<>(factory);
	}

	/**
	 * 상품 이벤트 토픽 생성
	 */
	@Bean
	public NewTopic productPostEventTopic() {
		return TopicBuilder
			.name(productPostEventTopicName)
			.partitions(topicPartitions)
			.replicas(topicReplicationFactors)
			.build();
	}

}