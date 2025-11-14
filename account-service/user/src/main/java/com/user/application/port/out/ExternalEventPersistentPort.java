package com.user.application.port.out;

import com.user.domain.vo.EventType;

public interface ExternalEventPersistentPort {

	void save(String event, EventType eventType);
	void completePublish(String userId, EventType eventType);
}
