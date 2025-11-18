package com.auth.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LogoutRedisDao {

	private static final String USER_LOGOUT_PREFIX = "logout:";
	private static final String VALUE = "logout:value";

	private final RedisTemplate<String, String> redisTemplate;

	public void addLogoutList(String token, long expiration) {
		try {
			String userKey = USER_LOGOUT_PREFIX + token;

			redisTemplate.opsForValue()
				.set(userKey, VALUE, System.currentTimeMillis() - expiration, TimeUnit.MILLISECONDS);

			log.info("로그아웃 토큰 저장 완료");
		} catch (Exception e) {
			log.error("로그아웃 토큰 저장 실패 - error: {}", e.getMessage(), e);
			throw new RuntimeException("로그아웃 토큰 저장에 실패했습니다.", e);
		}
	}

	public String findByToken(String token) {
		try {
			String userKey = USER_LOGOUT_PREFIX + token;
			return redisTemplate.opsForValue().get(userKey);

		} catch (Exception e) {
			log.error("블랙리스트 토큰 조회 실패 - error: {}", e.getMessage(), e);
			return null;
		}
	}
}
