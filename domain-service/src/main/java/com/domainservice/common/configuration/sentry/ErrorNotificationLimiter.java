package com.domainservice.common.configuration.sentry;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Slack에 동일한 에러 중복 알림 방지 기능을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorNotificationLimiter {

	private static final String KEY_PREFIX = "error:notification:";
	private static final int COOLDOWN_MINUTES = 10;

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 알림 전송 가능 여부 확인
	 */
	public boolean shouldNotify(Exception exception) {
		String key = generateKey(exception);

		try {

			Boolean isNew = redisTemplate.opsForValue()
				.setIfAbsent(key, "1", COOLDOWN_MINUTES, TimeUnit.MINUTES);

			return Boolean.TRUE.equals(isNew);

		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * 예외 타입 + 발생 위치로 고유 키 생성
	 */
	private String generateKey(Exception exception) {

		String type = exception.getClass().getSimpleName();
		String location = "unknown";

		if (exception.getStackTrace().length > 0) {
			StackTraceElement element = exception.getStackTrace()[0];
			location = element.getClassName() + "." + element.getMethodName();
		}

		return KEY_PREFIX + type + ":" + location;
	}
}