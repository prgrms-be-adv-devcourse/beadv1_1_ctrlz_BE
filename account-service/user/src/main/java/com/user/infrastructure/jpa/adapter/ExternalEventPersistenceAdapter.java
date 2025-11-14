package com.user.infrastructure.jpa.adapter;

import org.springframework.stereotype.Repository;

import com.user.application.port.out.ExternalEventPersistentPort;
import com.user.infrastructure.jpa.entity.ExternalEventEntity;
import com.user.infrastructure.jpa.repository.ExternalEventJpaRepository;
import com.user.domain.vo.EventType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ExternalEventPersistenceAdapter implements ExternalEventPersistentPort {

	private final ExternalEventJpaRepository externalEventJpaRepository;

	@Override
	public void save(String userId, EventType eventType) {
		externalEventJpaRepository.save(ExternalEventEntity.from(userId, eventType, eventType.getContentWithUserId(userId)));
	}
}
