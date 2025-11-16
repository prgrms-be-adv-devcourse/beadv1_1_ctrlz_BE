package com.accountapplication.user.infrastructure.kafka.producer;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.accountapplication.user.infrastructure.kafka.TestKafkaConsumer;
import com.user.domain.event.UserSignedUpEvent;
import com.user.domain.vo.EventType;
import com.user.infrastructure.kafka.producer.kafkaOutboundEventPublisher;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("test")
@EmbeddedKafka(
	kraft = true,
	partitions = 1,
	ports = 9092
)
class kafkaOutboundEventPublisherTest {

	@Autowired
	kafkaOutboundEventPublisher testKafkaOutboundEventPublisher;

	@Autowired
	TestKafkaConsumer testKafkaConsumer;

	@Value("${custom.cart.topic.command}")
	private String cartTopicCommand;


	@BeforeEach
	void setUp() {
		testKafkaConsumer.getTestStore().clear();
	}

	@DisplayName("kafka 카트 생성 이벤트를 발행할 수 있다.")
	@Test
	void test1() throws Exception {
		//given
		UserSignedUpEvent event = new UserSignedUpEvent("test_id", EventType.CREATED);

		//when
		testKafkaOutboundEventPublisher.publish(cartTopicCommand, event);
		Thread.sleep(1500);

		//then
		assertThat(testKafkaConsumer.getTestStore().size()).isEqualTo(1);
	}
}