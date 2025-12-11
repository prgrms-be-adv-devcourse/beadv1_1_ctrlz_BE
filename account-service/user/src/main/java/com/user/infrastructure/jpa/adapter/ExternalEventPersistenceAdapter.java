package com.user.infrastructure.jpa.adapter;

import java.util.Arrays;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.user.application.adapter.vo.CommandType;
import com.user.application.adapter.vo.EventType;
import com.user.application.port.out.ExternalEventPersistentPort;
import com.user.infrastructure.jpa.entity.ExternalEventEntity;
import com.user.infrastructure.jpa.exception.ExternalEventException;
import com.user.infrastructure.jpa.repository.ExternalEventJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Repository
public class ExternalEventPersistenceAdapter implements ExternalEventPersistentPort {

	private final ExternalEventJpaRepository externalEventJpaRepository;

	@Override
	public void save(String userId, String eventType, String... commandTypes) {
		Arrays.stream(commandTypes).forEach(commandType -> {
			externalEventJpaRepository.save(
				ExternalEventEntity.from(
					userId,
					eventType,
					commandType,
					"%s command userId: %s".formatted(commandType, userId)
				)
			);
		});
	}

	@Override
	public void completePublish(String userId, String eventType, String commandType) {

		ExternalEventEntity externalEvent =
			externalEventJpaRepository.findExternalEvent(userId, eventType,commandType)
				.orElseThrow(() -> new ExternalEventException("Event not found : " + userId));

		externalEvent.publishedComplete();
	}
}
