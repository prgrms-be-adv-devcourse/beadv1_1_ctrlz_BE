package com.user.infrastructure.batch;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.user.application.adapter.dto.CartCreateCommand;
import com.user.application.port.out.OutboundEventPublisher;
import com.user.infrastructure.jpa.entity.ExternalEventEntity;
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
	private final Set<String> processedUserIdSet = ConcurrentHashMap.newKeySet();

	@Override
	public void write(Chunk<? extends CartCreateCommand> chunk){
		List<? extends CartCreateCommand> items = chunk.getItems();
		log.info("Starting batch write for {} items", items.size());

		sendBatchToKafka(items);
		updatePublishedStatus();

		log.info("Batch write completed for {} items", items.size());
	}

	private void sendBatchToKafka(List<? extends CartCreateCommand> items) {
		for (CartCreateCommand command : items) {
			try {
				outboundEventPublisher.publish(cartCommandTopic, command);
				processedUserIdSet.add(command.userId());
				log.debug("Cart create command sent for userId: {}", command.userId());
			} catch (Exception e) {
				log.error("Failed to send cart create command for userId: {}", command.userId(), e);
			}
		}
	}


	private void updatePublishedStatus() {
		if (processedUserIdSet.isEmpty()) {
			log.warn("No user IDs to update");
			return;
		}

		try {

			List<ExternalEventEntity> eventsToUpdate =
				externalEventJpaRepository.findTop1000ByPublishedOrderByCreatedAtDesc(false);

			eventsToUpdate.forEach(ExternalEventEntity::publishedComplete);
			externalEventJpaRepository.saveAll(eventsToUpdate);

			log.info("Updated published status for {} events", eventsToUpdate.size());
			processedUserIdSet.clear();
		} catch (Exception e) {
			log.error("Error updating published status", e);
		}
	}
}
