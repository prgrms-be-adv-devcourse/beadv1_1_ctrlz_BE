package com.aiservice.domain.event;

public record ProductPostDeletedEvent(
	String postId,
	EventType eventType) {
}