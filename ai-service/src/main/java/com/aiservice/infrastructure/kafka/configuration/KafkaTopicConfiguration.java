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

	/**
	 * DLT 생성
	 * - 메시지 처리 실패 시 최종적으로 저장되는 토픽
	 * - 원본 토픽과 동일한 파티션/복제 수 사용으로 순서 보장
	 */
	@Bean
	public NewTopic createProductPostDLT() {
		return TopicBuilder.name(productPostTopic + ".DLT")
			.partitions(topicPartitions)
			.replicas(topicReplications)
			.build();
	}
}
