package com.domainservice;

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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConfiguration {

	@Value("${custom.cart.topic.command}")
	private String cartTopicCommand;

	@Value("${custom.config.topic-partitions}")
	private int topicPartitions;
	@Value("${custom.config.topic-replications}")
	private int topicReplications;

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;

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

	private Map<String, Object> commonConsumerConfig() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
		
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
		
		return props;
	}

	@Bean
	public ConsumerFactory<String, CartCreateCommand> cartConsumerFactory() {
		Map<String, Object> props = commonConsumerConfig();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CartCreateCommand.class.getName());
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CartCreateCommand> cartKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, CartCreateCommand> factory = 
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(cartConsumerFactory());
		return factory;
	}
}
