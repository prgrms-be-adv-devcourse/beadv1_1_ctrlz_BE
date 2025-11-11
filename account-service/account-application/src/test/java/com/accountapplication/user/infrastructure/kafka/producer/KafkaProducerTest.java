package com.accountapplication.user.infrastructure.kafka.producer;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import com.accountapplication.user.infrastructure.kafka.TestKafkaConsumer;
import com.accountapplication.user.infrastructure.kafka.config.TestKafkaProducer;
import com.user.application.adapter.event.CartCreatedEvent;
import com.user.application.adapter.event.DepositCreatedEvent;

@Disabled("kafka 도입 시 다시 테스트 합니다.")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("test")
@EmbeddedKafka(
	kraft = true,
	partitions = 1,
	ports = 9092
)
class KafkaProducerTest {

	// @Autowired
	TestKafkaProducer testKafkaProducer;

	// @Autowired
	TestKafkaConsumer testKafkaConsumer;

	@Value("${custom.cart.topic.command}")
	private String cartTopicCommand;

	@Value("${custom.deposit.topic.command}")
	private String depositTopicCommand;

	@BeforeEach
	void setUp() {
		testKafkaConsumer.getTestStore().clear();
	}

	@DisplayName("kafka 카트 생성 이벤트를 발행할 수 있다.")
	@Test
	void test1() throws Exception {
		//given
		CartCreatedEvent event = new CartCreatedEvent("test_id_cart");

		//when
		testKafkaProducer.send(cartTopicCommand, event);
		Thread.sleep(1500);

		//then
		assertThat(testKafkaConsumer.getTestStore().size()).isEqualTo(1);
	}

	@DisplayName("kafka 예치금 생성 이벤트를 발행할 수 있다.")
	@Test
	void test2() throws Exception {
		//given
		DepositCreatedEvent event = new DepositCreatedEvent("test_id_deposit");

		//when
		testKafkaProducer.send(depositTopicCommand, event);
		Thread.sleep(1500);

		//then
		assertThat(testKafkaConsumer.getTestStore().size()).isEqualTo(1);
	}

}