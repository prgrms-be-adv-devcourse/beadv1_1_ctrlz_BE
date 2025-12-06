package com.aiservice.infrastructure.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.aiservice.application.SessionService;
import com.aiservice.domain.model.RecommendationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	//TODO: 이 내역도 저장해야되나????
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
