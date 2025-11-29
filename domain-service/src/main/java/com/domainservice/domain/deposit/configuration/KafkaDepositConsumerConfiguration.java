package com.domainservice.domain.deposit.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.common.event.DepositCreateCommand;

@Configuration
public class KafkaDepositConsumerConfiguration {

	@Value("${custom.deposit.topic.command}")
	private String depositTopicCommand;

	@Value("${custom.config.topic-partitions}")
	private int topicPartitions;
	@Value("${custom.config.topic-replications}")
	private int topicReplications;

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;

	@Bean
	public NewTopic createDepositCommandTopic() {
		return TopicBuilder.name(depositTopicCommand)
			.partitions(topicPartitions)
			.replicas(topicReplications)
			.build();
	}

	@Bean
	public ConsumerFactory<String, DepositCreateCommand> depositConsumerFactory() {
		Map<String, Object> props = depositConsumerConfig();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DepositCreateCommand.class.getName());
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, DepositCreateCommand> depositKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, DepositCreateCommand> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(depositConsumerFactory());

		// acknowledge() 메서드를 호출한 즉시 커밋
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

		DefaultErrorHandler errorHandler = new DefaultErrorHandler(
			new FixedBackOff(1000L, 1)
		);

		// Micrometer Observation을 통한 트레이스 전파
		factory.getContainerProperties().setObservationEnabled(true);

		factory.setCommonErrorHandler(errorHandler);
		return factory;
	}

	private Map<String, Object> depositConsumerConfig() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // acknowledge() 수동 호출

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

		props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

		return props;
	}
}
