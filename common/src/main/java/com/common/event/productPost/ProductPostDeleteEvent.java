package com.common.event.productPost;

public record ProductPostDeleteEvent(
	String postId,
	EventType eventType
) {
}