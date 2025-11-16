package com.user.application.port.out;

public interface OutboundEventPublisher {

	<T> void publish(String topicName, T event);
}
