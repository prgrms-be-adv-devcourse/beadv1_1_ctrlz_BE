package com.user.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.infrastructure.jpa.entity.ExternalEventEntity;

public interface ExternalEventJpaRepository extends JpaRepository<ExternalEventEntity, String> {
}
