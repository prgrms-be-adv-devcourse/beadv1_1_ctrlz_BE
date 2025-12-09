package com.domainservice.domain.search.service.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import com.common.annotation.KafkaProducer;
import com.common.event.SearchWordSavedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@KafkaProducer
@RequiredArgsConstructor
public class SearchWordEventProducer {

	@Value("${custom.search.topic.event}")
	private String searchWordEventTopic;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publishSearchWordEventToAi(String word, String userId) {
		kafkaTemplate.send(searchWordEventTopic, new SearchWordSavedEvent(word, userId));
	}

}
