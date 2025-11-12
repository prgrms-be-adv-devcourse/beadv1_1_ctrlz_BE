package com.auth.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RefreshTokenRedisRepository implements TokenRepository {

	@Value("${jwt.refresh-expiration}")
	private Long refreshExpire;

	private static final String KEY_PREFIX = "refresh_token:";

	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public void save(String userId, String token) {
		try {

			String key = KEY_PREFIX + userId;
			if(redisTemplate.hasKey(key)){
				redisTemplate.delete(key);
			}

			redisTemplate.opsForValue().set(key, token, refreshExpire, TimeUnit.MILLISECONDS);
			log.info("Refresh token saved for userId: {}", userId);

		} catch (Exception e) {
			log.error("리프레시 토큰 저장 실패 userId: {}", userId, e);
			throw new IllegalStateException("Refresh token 저장 실패", e);
		}
	}

	@Override
	public String findByUserId(String userId) {
		try {
			String key = KEY_PREFIX + userId;
			String token = redisTemplate.opsForValue().get(key);
			if (token != null) {
				return token;
			}
			log.warn("No token found for userId: {}", userId);
			throw new IllegalStateException("리프레시 토큰이 없습니다." + userId);
		} catch (Exception e) {
			log.error("Failed to find token for userId: {}", userId, e);
			throw new RuntimeException("토큰 조회 실패", e);
		}
	}

	@Override
	public void deleteByUserId(String userId) {
		try {
			String key = KEY_PREFIX + userId;
			Boolean deleted = redisTemplate.delete(key);
			if (deleted) {
				log.info("리프레시 토큰이 삭제되었습니다. userId: {}", userId);
			} else {
				log.warn("리프레시 토큰을 찾을 수 없습니다. userId: {}", userId);
			}
		} catch (Exception e) {
			log.error("Failed to delete refresh token for userId: {}", userId, e);
			throw new RuntimeException("Refresh token 삭제 실패", e);
		}
	}
}
