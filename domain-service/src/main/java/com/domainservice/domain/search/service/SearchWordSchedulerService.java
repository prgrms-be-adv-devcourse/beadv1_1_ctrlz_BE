package com.domainservice.domain.search.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import com.common.annotation.TimeTrace;
import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;
import com.domainservice.domain.search.repository.jpa.SearchWordLogCommandRepository;
import com.domainservice.domain.search.repository.jpa.SearchWordLogQueryRepository;

import com.domainservice.domain.search.repository.redis.PopularSearchWordRedisRepository;
import com.domainservice.domain.search.repository.redis.SearchLogRedisRepository;
import com.domainservice.domain.search.service.dto.vo.DailyPopularWordLog;
import com.domainservice.domain.search.service.dto.vo.KeywordLog;
import com.domainservice.domain.search.util.TimeUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchWordSchedulerService {

	private final PopularSearchWordRedisRepository popularWordRedisRepository;
	private final SearchLogRedisRepository searchLogRedisRepository;
	private final SearchWordLogQueryRepository searchWordLogQueryRepository;
	private final SearchWordLogCommandRepository searchWordLogCommandRepository;

	/**
	 * 5분마다 실시간 인기 검색어 업데이트
	 */
	@Transactional(readOnly = true)
	public List<KeywordLog> getTrendWordLogList() {
		return popularWordRedisRepository.findRealTimeTrendWordLog();
	}

	@TimeTrace
	@Transactional
	public void updateRealtimeTrendWordList(List<KeywordLog> log) {
		popularWordRedisRepository.updateRealTimeTrendWord(log);
	}

	@Transactional(readOnly = true)
	public List<KeywordLog> collectDailyPopularRedisLog() {
		return popularWordRedisRepository.findDailyPopularWordListLog();
	}

	@Transactional(readOnly = true)
	public Map<String, DailyPopularWordLog> getPreviousDailyPopularDBLog() {
		//2시간 전에 저장된 로그 조회
		LocalDateTime lastBatchedAt = TimeUtils.getLastBatchExecutionTime().minusHours(2);
		return searchWordLogQueryRepository.findDailyLogs(lastBatchedAt);
	}

	@TimeTrace
	@Transactional
	public void updateDailyPopularWordList(List<KeywordLog> logList, Map<String, DailyPopularWordLog> previousLogMap) {
		popularWordRedisRepository.decayDailyPopularScores();

		logList
			.forEach(log -> {
					DailyPopularWordLog previousLog = previousLogMap.get(log.keyword());
					popularWordRedisRepository.updateDailyPopularWordList(log, Optional.ofNullable(previousLog));
				}
			);

	}

	/**
	 * 일간 인기 검색어 업데이트
	 * 1. 로그를 가져온다.
	 * 2. DB에서 이전 타임에 저장된 로그를 가져온다.
	 * 3.
	 */
	@Transactional
	public void updateElasticsearch() {
		throw new UnsupportedOperationException();
	}

	@TimeTrace
	@Transactional
	public void saveLogsToDataBase(List<KeywordLog> keywordLogs) {
		//Bulk-Insert 적용 예정
		List<SearchWordLog> allLogs =
			keywordLogs.stream()
				.flatMap(keywordLog ->
					keywordLog.searchedAt().stream()
						.map(searchedAt ->
							SearchWordLog.create(keywordLog.keyword(), searchedAt)
						)
				)
				.toList();

		// Bulk insert 1번
		searchWordLogCommandRepository.insertAll(allLogs);
	}
}
