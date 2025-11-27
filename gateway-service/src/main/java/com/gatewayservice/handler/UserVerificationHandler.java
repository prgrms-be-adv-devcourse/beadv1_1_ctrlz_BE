package com.gatewayservice.handler;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserVerificationHandler {

	@Value("${jwt.expiration}")
	private long expiration;

	private static final String IP_KEY = "request:ip:";
	private final RedisTemplate<String, String> redisTemplate;

	public void addTokenAndIp(String token, String ip) {
		redisTemplate.opsForValue().set(IP_KEY + ip, token, expiration, TimeUnit.MILLISECONDS);
	}

	public boolean validateToken(String ip,String token) {
		String accessToken = redisTemplate.opsForValue().get(IP_KEY + ip);
		return token.equals(accessToken);
	}
}
