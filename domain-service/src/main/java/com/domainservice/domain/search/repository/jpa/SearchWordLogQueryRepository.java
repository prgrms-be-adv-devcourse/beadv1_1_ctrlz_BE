package com.domainservice.domain.search.repository.jpa;

import java.time.LocalDateTime;
import java.util.Map;

import com.domainservice.domain.search.service.dto.vo.DailyPopularWordLog;

public interface SearchWordLogQueryRepository {

	Map<String, DailyPopularWordLog> findDailyLogs(LocalDateTime end);
}
