package com.user.application.port.out;

import java.util.List;

import com.user.domain.vo.EventType;
import com.user.infrastructure.scheduler.configuration.vo.PendingEvent;

public interface ExternalEventPersistentPort {

	void save(String event, EventType eventType);
	void completePublish(String userId, EventType eventType);
	List<PendingEvent> findPendingEvents();
}
