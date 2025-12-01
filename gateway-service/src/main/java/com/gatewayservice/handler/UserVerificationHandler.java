package com.gatewayservice.handler;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserVerificationHandler {

	//여유분
	public static final long EXPIRATION_LEEWAY = 2000L;

	@Value("${jwt.expiration}")
	private long expiration;

	private static final String IP_KEY = "request:ip:";
	private final RedisTemplate<String, String> redisTemplate;

	public void addTokenAndIp(String token, String ip) {
		redisTemplate.opsForValue().set(IP_KEY + ip, token, expiration + EXPIRATION_LEEWAY, TimeUnit.MILLISECONDS);
	}

	public boolean verifyTokenWithIp(String ip, String token) {
		String accessToken = redisTemplate.opsForValue().get(IP_KEY + ip);
		if (accessToken == null) {
			return true;
		}
		return !token.equals(accessToken);
	}
}
