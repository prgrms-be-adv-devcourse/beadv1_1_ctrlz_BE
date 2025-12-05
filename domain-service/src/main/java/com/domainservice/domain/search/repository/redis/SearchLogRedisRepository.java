package com.domainservice.domain.search.repository.redis;

import java.time.Instant;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.domainservice.domain.search.model.vo.SearchWord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchLogRedisRepository {

	private final RedisTemplate<String, String> redisTemplate;
	private static final String USER_SEARCH_PREFIX = "search:user:";

	/**
	 * 유저별 최근 검색어 저장 (ZSET — timestamp score)
	 */
	public void save(SearchWord word, String userKey) {
		String userSearchKey = USER_SEARCH_PREFIX + userKey;

		// 검색 순간 timestamp (millis)
		double score = Instant.now().toEpochMilli();

		redisTemplate.opsForZSet().add(userSearchKey, word.value(), score);

		// 만료일 30일
		redisTemplate.expire(userSearchKey, java.time.Duration.ofDays(30));
	}

	/**
	 * 유저 최근 검색어 10개 조회 (가장 최근순)
	 */
	public List<SearchWord> findByUserKey(String userKey) {
		String userSearchKey = USER_SEARCH_PREFIX + userKey;

		List<String> results = redisTemplate
			.opsForZSet()
			.reverseRange(userSearchKey, 0, 9)  // 가장 최근 10개
			.stream()
			.toList();

		return results.stream()
			.map(SearchWord::new)
			.toList();
	}
}
