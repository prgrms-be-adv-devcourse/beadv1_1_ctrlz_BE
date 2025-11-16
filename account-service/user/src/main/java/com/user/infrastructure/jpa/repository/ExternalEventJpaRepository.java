package com.user.infrastructure.jpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.domain.vo.EventType;
import com.user.infrastructure.jpa.entity.ExternalEventEntity;

public interface ExternalEventJpaRepository extends JpaRepository<ExternalEventEntity, String> {
	Optional<ExternalEventEntity> findExternalEventEntitiesByUserIdAndEventType(String userId, EventType eventType);
	List<ExternalEventEntity> findTop20ByPublishedOrderByCreatedAt(boolean published);
}
