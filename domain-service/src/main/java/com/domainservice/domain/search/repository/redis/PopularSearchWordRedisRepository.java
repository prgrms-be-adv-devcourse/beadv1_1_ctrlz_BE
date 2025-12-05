package com.domainservice.domain.search.repository.redis;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import com.domainservice.domain.search.model.dto.response.SearchWordResponse;
import com.domainservice.domain.search.model.vo.SearchWord;
import com.domainservice.domain.search.service.converter.PrefixConverter;
import com.domainservice.domain.search.service.dto.vo.DailyPopularWordLog;
import com.domainservice.domain.search.service.dto.vo.KeywordLog;
import com.domainservice.domain.search.util.TimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PopularSearchWordRedisRepository {

	private final RedisTemplate<String, String> redisTemplate; //<String, >
	private static final String DAILY_POPULAR_PREFIX = "search:popular:daily:";
	private static final String SEARCHED_AT_KEY_PREFIX = "search:trend:searchedAt:";

	private static final String TREND_ZSET_KEY = "search:trend:zset";
	private static final String DAILY_ZSET_KEY = "search:daily:zset";		//일간 인기 검색어



	/**
	 * 검색어 발생 시 Redis에 4가지 이벤트 기록
	 * 1. 모든 키워드 목록 보관
	 * 2. 실시간 트렌드 계산용 5분 카운트 증가
	 * 3. 실시간 트렌드 계산용 마지막에 검색된 시간 저장
	 * 4. 일간 인기 키워드 증가
	 */
	public void save(SearchWord word) {
		String keyword = word.value();

		// 1) 키워드 - 시간 리스트
		LocalDateTime now = LocalDateTime.now();
		String timeKey = TimeUtils.convertDateTimeToString(now);
		String searchedAtKey = SEARCHED_AT_KEY_PREFIX + keyword;
		redisTemplate.opsForList()
			.leftPush(searchedAtKey, timeKey);

		// 2) 일간 인기 키워드 Count 증가
		String dailyKey = DAILY_POPULAR_PREFIX +  keyword;
		redisTemplate.opsForList().leftPush(dailyKey, timeKey);
		redisTemplate.expire(dailyKey, Duration.ofHours(2).plusMinutes(10));
	}


	/**
	 * 실시간 인기 검색어 로그 조회하기(업데이트용)
	 * scheduler 사용예정
	 * @return
	 */
	public List<KeywordLog> findRealTimeTrendWordLog() {
		Set<String> searchedAtKeys = getKeysByPattern(SEARCHED_AT_KEY_PREFIX +"*");

		return searchedAtKeys.stream()
			.map(key -> {
				List<String> timestamps = redisTemplate.opsForList().range(key, 0, -1);
				redisTemplate.delete(key);

				String keyword = key.replace(SEARCHED_AT_KEY_PREFIX, ""); // trend:searchedAt:keyword

				List<LocalDateTime> searchedAtList = timestamps.stream()
					.map(TimeUtils::convertFromStringToDateTime)
					.toList();

				return new KeywordLog(
					keyword,
					searchedAtList,
					null
				);
			})
			.toList();
	}

	/**
	 * 실시간 인기 검색어 리스트 업데이트
	 * (5분 마다 진행)
	 */
	public void updateRealTimeTrendWord(List<KeywordLog> wordList) {
		// 1) 기존 점수 감쇠
		double tauMillis = 20 * 60 * 1000.0;   // 20분 서서히 죽이기
		double intervalMillis = 5 * 60 * 1000.0; // 배치 주기 5분
		double epsilon = 0.01;                 // 이보다 작으면 제거

		applyDecayToZSet(TREND_ZSET_KEY, tauMillis, intervalMillis, epsilon);

		// 2) 이번 5분 동안 검색된 횟수 반영
		for (KeywordLog keywordLog : wordList) {
			String keyword = keywordLog.keyword();
			long currentCount = keywordLog.searchedAt().size();
			if (currentCount <= 0) continue;

			// 기존 점수에 currentCount 만큼 더하기
			redisTemplate.opsForZSet()
				.incrementScore(TREND_ZSET_KEY, keyword, currentCount);
		}
	}


	/**
	 * 실시간 인기 검색어 조회 메서드
	 * @return
	 */
	public List<SearchWordResponse> findTrendWordList() {
		return getSearchWordResponsesByKey(TREND_ZSET_KEY);
	}

	private List<SearchWordResponse> getSearchWordResponsesByKey(String key) {
		Set<ZSetOperations.TypedTuple<String>> items =
			redisTemplate.opsForZSet()
				.reverseRangeWithScores(key, 0, 9);

		if (items == null || items.isEmpty()) {
			return List.of();
		}

		return items.stream()
			.map(tuple -> {
				String value = tuple.getValue();
				return new SearchWordResponse(
					value,              // keyword
					PrefixConverter.convertToQwertyInput(value)
				);
			})
			.toList();
	}

	/**
	 * 2시간동안 조회된 검색어 목록 반환.
	 * 조회수를 기준으로
	 * @return
	 */
	public List<KeywordLog> findDailyPopularWordListLog() {
		Set<String> keys = getKeysByPattern(DAILY_POPULAR_PREFIX + "*");

		return keys.stream().map(key -> { 					//search:popular:daily:단어
			String keyword = key.replace(DAILY_POPULAR_PREFIX, "");

			List<LocalDateTime> searchedAtList = redisTemplate.opsForList().range(key, 0, -1).stream()
				.map(TimeUtils::convertFromStringToDateTime)
				.toList();

			redisTemplate.delete(key);

			return new KeywordLog(
				keyword,
				searchedAtList,
				TimeUtils.getCurrentBatchExecutionTime()
			);
		}).toList();
	}

	private Set<String> getKeysByPattern(String keyPattern) {
		Set<String> keys = new HashSet<>();
		ScanOptions scanOptions = ScanOptions.scanOptions()
			.match(keyPattern)    // trend:count:5m:*
			.build();

		try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
			while (cursor.hasNext()) {
				keys.add(cursor.next());
			}
		}

		return keys;
	}

	/**
	 * 일간 인기 검색어 점수 감쇠 (2시간 배치에서 호출)
	 */
	public void decayDailyPopularScores() {
		double tauMillis = 6 * 60 * 60 * 1000.0;  // 6시간 정도로 서서히 죽이기
		double intervalMillis = 2 * 60 * 60 * 1000.0; // 2시간 배치
		double epsilon = 0.1;                      // 일간은 좀 더 크게 잡아도 됨

		applyDecayToZSet(DAILY_ZSET_KEY, tauMillis, intervalMillis, epsilon);
	}


	/**
	 * 일간 인기 검색어 리스트 반환
	 * @return
	 */
	public List<SearchWordResponse> findDailyPopularWord() {
		return getSearchWordResponsesByKey(DAILY_ZSET_KEY);
	}

	/**
	 * 일간 인기 검색어 리스트를 Redis에 update
	 * @param currentLog {검색어, 검색 시간 로그들, 배치가 실행된 시각}
	 * @param previousLog {검색어, 이전 스케줄러에서 집계된 검색 횟수(2시간 단위), 마지막으로 검색된 시간}
	 */
	public void updateDailyPopularWordList(KeywordLog currentLog, Optional<DailyPopularWordLog> previousLog) {

		String keyword = currentLog.keyword();

		// 이번 배치에서 검색된 횟수
		long currentCount = currentLog.searchedAt().size();

		// 만약 이전 배치에 정보가 없다면 score가 없을 수도 있음
		// redis에서 기존 점수 조회
		Double prevScore = redisTemplate.opsForZSet().score(DAILY_ZSET_KEY, keyword);
		double safePrevScore = prevScore != null ? prevScore : 0.0;

		// 새로운 점수 = 기존 점수 + 이번 배치 수치
		double newScore = safePrevScore + currentCount;

		// ZSET 업데이트 (setScore)
		redisTemplate.opsForZSet().add(DAILY_ZSET_KEY, keyword, newScore);

		log.info("[DAILY POPULAR] keyword={}, prevScore={}, currentCount={}, newScore={}",
			keyword, safePrevScore, currentCount, newScore);
	}

	/**
	 * Redis에 저장된 모든 key-value 조회
	 */
	public Map<String, Object> findAll() {
		Map<String, Object> result = new HashMap<>();

		ScanOptions scanOptions = ScanOptions.scanOptions()
			.match("*")
			.count(1000)
			.build();

		try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
			.getConnection()
			.scan(scanOptions)) {

			while (cursor.hasNext()) {
				String key = new String(cursor.next());
				DataType type = redisTemplate.type(key);

				switch (type) {
					case STRING -> {
						String value = redisTemplate.opsForValue().get(key);
						result.put(key, value);
					}
					case LIST -> {
						List<String> list = redisTemplate.opsForList().range(key, 0, -1);
						result.put(key, list);
					}
					case SET -> {
						Set<String> set = redisTemplate.opsForSet().members(key);
						result.put(key, set);
					}
					case ZSET -> {
						Set<ZSetOperations.TypedTuple<String>> zset =
							redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
						result.put(key, zset);
					}
					case HASH -> {
						Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
						result.put(key, hash);
					}
					default -> {
						// 기타(type none 등)
						result.put(key, "UNSUPPORTED TYPE: " + type);
					}
				}
			}
		}

		return result;
	}

	private void applyDecayToZSet(String zsetKey,
		double tauMillis,       // 감쇠 시간 상수
		double intervalMillis,  // 배치 주기
		double epsilon) {       // 너무 작으면 지울 임계값
		double decayFactor = Math.exp(-intervalMillis / tauMillis);

		Set<ZSetOperations.TypedTuple<String>> existing =
			redisTemplate.opsForZSet().rangeWithScores(zsetKey, 0, -1);

		if (existing == null || existing.isEmpty()) {
			return;
		}

		for (ZSetOperations.TypedTuple<String> tuple : existing) {
			String keyword = tuple.getValue();
			Double score = tuple.getScore();
			if (keyword == null || score == null) continue;

			double decayed = score * decayFactor;

			if (decayed < epsilon) {
				// 점수가 너무 작으면 제거
				redisTemplate.opsForZSet().remove(zsetKey, keyword);
			} else {
				// 감쇠된 점수로 갱신
				redisTemplate.opsForZSet().add(zsetKey, keyword, decayed);
			}
		}
	}


}
