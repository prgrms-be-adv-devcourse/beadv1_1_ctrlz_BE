package com.userservice.infrastructure.kafka.producer;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import com.userservice.infrastructure.kafka.TestKafkaConsumer;
import com.userservice.infrastructure.kafka.event.CartCreatedEvent;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(
	kraft = true,
	partitions = 1,
	ports = 9092
)
class KafkaProducerTest {

	@Autowired
	KafkaProducer kafkaProducer;

	@Autowired
	TestKafkaConsumer testKafkaConsumer;

	@Value("${custom.cart.topic.command}")
	private String cartTopicCommand;

	@DisplayName("kafka 프로듀싱을 할 수 있다?")
	@Test
	void test1() throws Exception {
		//given
		CartCreatedEvent event = new CartCreatedEvent("test_id");

		//when
		kafkaProducer.send(cartTopicCommand, event);
		Thread.sleep(500);

		//then
		assertThat(testKafkaConsumer.getTestStore().size()).isEqualTo(1);
	}

}