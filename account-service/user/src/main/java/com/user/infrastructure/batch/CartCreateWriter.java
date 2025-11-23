package com.user.infrastructure.batch;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.user.application.adapter.dto.CartCreateCommand;
import com.user.application.port.out.OutboundEventPublisher;
import com.user.infrastructure.jpa.repository.ExternalEventJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCreateWriter implements ItemWriter<CartCreateCommand> {

	@Value("${custom.cart.topic.command}")
	private String cartCommandTopic;

	private final OutboundEventPublisher outboundEventPublisher;
	private final ExternalEventJpaRepository externalEventJpaRepository;

	@Transactional
	@Override
	public void write(Chunk<? extends CartCreateCommand> chunk) {
		List<? extends CartCreateCommand> items = chunk.getItems();
		log.info("Starting batch write for {} items", items.size());

		Set<String> successUserId = sendBatchToKafka(items);
		updatePublishedStatus(successUserId);

		log.info("Batch write completed for {} items", items.size());
	}

	private Set<String> sendBatchToKafka(List<? extends CartCreateCommand> items) {
		Set<String> successfulUserIds = ConcurrentHashMap.newKeySet();
		for (CartCreateCommand command : items) {
			publishByKafka(command, successfulUserIds);
		}
		return successfulUserIds;
	}

	private void publishByKafka(CartCreateCommand command, Set<String> successfulUserIds) {
		try {
			outboundEventPublisher.publish(cartCommandTopic, command);
			successfulUserIds.add(command.userId());
			log.debug("Cart create command sent for userId: {}", command.userId());
		} catch (Exception e) {
			log.warn("카프카 전송 실패 userId: {}", command.userId(), e);
		}
	}

	private void updatePublishedStatus(Set<String> successUserId) {
		if (successUserId.isEmpty()) {
			log.info("전송할 userId가 없습니다.");
			return;
		}

		try {
			int count = externalEventJpaRepository.updatePublished(successUserId);
			log.info("이벤트 상태 업데이트 개수 : {}", count);
		} catch (Exception e) {
			log.error("Error updating published status", e);
			throw new RuntimeException("Failed to update published status", e);
		}
	}
}
