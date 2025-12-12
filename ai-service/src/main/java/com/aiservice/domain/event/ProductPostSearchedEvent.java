package com.aiservice.domain.event;

import lombok.Builder;

@Builder
public record ProductPostSearchedEvent(
	String userId,
	String query
) {
}

