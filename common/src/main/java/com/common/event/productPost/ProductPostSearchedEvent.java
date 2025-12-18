package com.common.event.productPost;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ProductPostSearchedEvent(
	String userId,
	String query,
	LocalDateTime timestamp
) {
}

