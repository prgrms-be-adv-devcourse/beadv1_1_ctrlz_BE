package com.common.event.productPost;

public record ProductPostDeletedEvent(
	String postId,
	EventType eventType
) {
}