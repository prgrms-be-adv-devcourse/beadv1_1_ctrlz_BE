package com.accountapplication.user.infrastructure.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Disabled("kafka 도입 시 다시 테스트 합니다.")
// @Profile("test || local")
// @Configuration
public class TestKafkaProducerConfiguration {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> properties = new HashMap<>();

		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 15000);
		properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
		properties.put("allow.auto.create.topics", false);

		return new DefaultKafkaProducerFactory<>(properties);

	}

}
