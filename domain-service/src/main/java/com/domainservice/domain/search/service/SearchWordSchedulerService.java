package com.domainservice.domain.search.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import com.common.annotation.TimeTrace;
import com.domainservice.domain.search.model.entity.dto.document.SearchWordDocumentEntity;
import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;
import com.domainservice.domain.search.repository.SearchWordLogCommandRepository;
import com.domainservice.domain.search.repository.SearchWordRepository;
import com.domainservice.domain.search.repository.jpa.SearchWordLogQueryRepository;

import com.domainservice.domain.search.repository.redis.PopularSearchWordRedisRepository;
import com.domainservice.domain.search.service.dto.vo.DailyPopularWordLog;
import com.domainservice.domain.search.service.dto.vo.KeywordLog;
import com.domainservice.domain.search.util.TimeUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchWordSchedulerService {

	private final PopularSearchWordRedisRepository popularWordRedisRepository;
	private final SearchWordLogQueryRepository searchWordLogQueryRepository;
	private final SearchWordLogCommandRepository searchWordLogCommandRepository;

	private final SearchWordRepository searchWordRepository;

	/**
	 * 실시간 인기 검색어 조회
	 */
	@Transactional(readOnly = true)
	public List<KeywordLog> getRealTimeTrendWordLogList() {
		return popularWordRedisRepository.findRealTimeTrendWordLog();
	}

	/**
	 * 실시간 인기 검색어 목록 최신화
	 * @param log
	 */
	@TimeTrace
	@Transactional
	public void updateRealtimeTrendWordList(List<KeywordLog> log) {
		popularWordRedisRepository.updateRealTimeTrendWord(log);
	}

	/**
	 * Redis에 저장된 일간 인기 검색어 log 목록 조회
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<KeywordLog> getDailyPopularRedisLog() {
		return popularWordRedisRepository.findDailyPopularWordListLog();
	}

	/**
	 * DB에 저장된 검색 로그 조회(현재 ~ 2시간 전)
	 * @return
	 */
	@Transactional(readOnly = true)
	public Map<String, DailyPopularWordLog> getPreviousDailyPopularDBLog() {
		//2시간 전에 저장된 로그 조회
		LocalDateTime lastBatchedAt = TimeUtils.getLastBatchExecutionTime();
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
	 * DB -> Elasticsearch 최신화
	 * Kafka Connect 방식 고려
	 */
	@Transactional
	public void updateElasticsearch() {
		throw new UnsupportedOperationException();
	}

	/**
	 * DB에 검색 로그 저장 - bulk-insert 적용
	 * @param keywordLogs
	 */
	@TimeTrace
	@Transactional
	public List<SearchWordLog> saveLogsToDataBase(List<KeywordLog> keywordLogs) {
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
		return searchWordLogCommandRepository.insertAll(allLogs);
	}

	/**
	 * SearchWordDocumentEntity의 originValue를 기준으로 데이터가 존재하면 update 없으면 insert하는 메서드
	 * @param savedLogs
	 */
	@TimeTrace
	@Transactional
	public void upsertLogsToElasticsearch(List<SearchWordLog> savedLogs) {

		searchWordRepository.upsertByOriginValue(
			savedLogs.stream()
				.map(SearchWordDocumentEntity::createDocumentEntity)
				.toList()
		);
	}
}
