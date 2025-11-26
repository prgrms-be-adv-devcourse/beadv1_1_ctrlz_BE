package com.user.infrastructure.jpa.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.user.application.port.out.ExternalEventPersistentPort;
import com.user.domain.vo.EventType;
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
	public void save(String userId, EventType eventType) {
		externalEventJpaRepository.save(
			ExternalEventEntity.from(userId, eventType, eventType.getContentWithUserId(userId)));
	}

	@Override
	public void completePublish(String userId, EventType eventType) {

		ExternalEventEntity externalEvent = externalEventJpaRepository.findExternalEventEntitiesByUserIdAndEventType(
				userId, eventType)
			.orElseThrow(() -> new ExternalEventException("Event not found : " + userId));
		externalEvent.publishedComplete();
	}
}
