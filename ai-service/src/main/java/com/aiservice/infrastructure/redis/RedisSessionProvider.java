package com.aiservice.infrastructure.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.aiservice.application.SessionService;
import com.aiservice.domain.model.RecommendationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Deprecated 프론트엔드 진행 작업에 따라 사용 여부가 달라집니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSessionProvider implements SessionService {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String KEY_PREFIX = "recommend:user:";
	private static final String COUNT_KEY_PREFIX = "recommend:count:";
	private static final Duration TTL = Duration.ofMinutes(30);

	@Override
	public void publishRecommendationData(String userId, RecommendationResult result) {
		String key = KEY_PREFIX + userId;
		redisTemplate.opsForValue().set(key, result, TTL);

		// Pub/Sub로 추천 결과 발행
		String channel = "recommendation:" + userId;
		redisTemplate.convertAndSend(channel, result);
		log.info("채널로 추천 결과 발행: {}", channel);
	}

	@Override
	public RecommendationResult getRecommendations(String userId) {
		String key = KEY_PREFIX + userId;
		return (RecommendationResult)redisTemplate.opsForValue().get(key);
	}

	@Override
	public void incrementRecommendationCount(String userId) {
		String key = COUNT_KEY_PREFIX + userId;
		Long count = redisTemplate.opsForValue().increment(key);
		if (count != null && count == 1) {
			redisTemplate.expire(key, TTL);
		}
	}

	@Override
	public int getRecommendationCount(String userId) {
		String key = COUNT_KEY_PREFIX + userId;
		Integer value = (Integer)redisTemplate.opsForValue().get(key);
		if (value == null) {
			return 0;
		}
		return value;
	}
}
