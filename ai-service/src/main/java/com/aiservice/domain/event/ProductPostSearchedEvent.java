package com.aiservice.domain.event;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProductPostSearchedEvent(
		String userId,
		String query,
		LocalDateTime timestamp
) {
}

