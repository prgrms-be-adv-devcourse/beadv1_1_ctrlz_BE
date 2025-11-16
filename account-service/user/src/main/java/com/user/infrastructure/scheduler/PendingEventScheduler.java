package com.user.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.user.application.port.in.PendingEventUseCase;
import com.user.infrastructure.jpa.repository.ExternalEventJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Component
public class PendingEventScheduler {

	private final ExternalEventJpaRepository externalEventJpaRepository;
	private final PendingEventUseCase pendingEventUseCase;

	@Scheduled(cron = "0 * * * * *")
	@Transactional
	public void publishPendingEvent() {
		pendingEventUseCase.publishPendingEvents();
	}
}
