package com.user.infrastructure.jpa.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.user.infrastructure.jpa.entity.ExternalEventEntity;

public interface ExternalEventJpaRepository extends JpaRepository<ExternalEventEntity, String> {

	@Query("select e from ExternalEventEntity e where e.userId = :userId and e.eventType = :eventType and e.commandType = :commandType")
	Optional<ExternalEventEntity> findExternalEvent(
		@Param("userId") String userId,
		@Param("eventType") String eventType,
		@Param("commandType") String commandType
	);

	@Modifying
	@Query("update ExternalEventEntity e set e.published = true where e.userId IN :userIds and e.published = false")
	int updatePublished(@Param("userIds") Set<String> userIds);
}
