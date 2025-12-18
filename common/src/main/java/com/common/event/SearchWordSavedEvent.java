package com.common.event;

public record SearchWordSavedEvent(
	String userId,
	String query
) {
}
