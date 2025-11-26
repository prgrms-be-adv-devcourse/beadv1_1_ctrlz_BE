package com.user.infrastructure.jpa.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.user.domain.vo.EventType;
import com.user.infrastructure.jpa.entity.ExternalEventEntity;

public interface ExternalEventJpaRepository extends JpaRepository<ExternalEventEntity, String> {
	Optional<ExternalEventEntity> findExternalEventEntitiesByUserIdAndEventType(String userId, EventType eventType);

	@Modifying
	@Query("update ExternalEventEntity e set e.published = true where e.userId IN :userIds and e.published = false")
	int updatePublished(@Param("userIds") Set<String> userIds);
}
