package com.domainservice.domain.search.service.dto.vo;

import java.time.LocalDateTime;

public record DailyPopularWordLog(
	String word,
	Integer searchedCount,
	LocalDateTime lastSearchedAt
) {
}
