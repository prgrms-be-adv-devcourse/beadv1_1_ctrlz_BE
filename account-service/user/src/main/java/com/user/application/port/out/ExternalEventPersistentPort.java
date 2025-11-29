package com.user.application.port.out;

public interface ExternalEventPersistentPort {

	void save(String event, String eventType, String... commandTypes);
	void completePublish(String userId, String eventType, String commandType);
}
