package com.domainservice.domain.search.service.dto.vo;

import java.time.LocalDateTime;
import java.util.List;

public record KeywordLog(
	String keyword,
	List<LocalDateTime> searchedAt,
	LocalDateTime batchedAt
) {
}
