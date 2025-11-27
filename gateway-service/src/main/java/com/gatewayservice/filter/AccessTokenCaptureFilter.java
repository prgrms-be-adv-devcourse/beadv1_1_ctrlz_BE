package com.gatewayservice.filter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class AccessTokenCaptureFilter extends AbstractGatewayFilterFactory<AccessTokenCaptureFilter.Config>
	implements Ordered {

	@Value("${jwt.expiration}")
	private long expiration;

	private static final String IP_KEY = "request:ip:";
	private final RedisTemplate<String, String> redisTemplate;

	public AccessTokenCaptureFilter(RedisTemplate<String, String> redisTemplate) {
		super(AccessTokenCaptureFilter.Config.class);
		this.redisTemplate = redisTemplate;
	}

	public static class Config {
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
			String requestId = exchange.getAttribute("REQUEST_ID");
			if (requestId == null) {
				return;
			}

			ServerHttpResponse response = exchange.getResponse();
			String accessToken = extractAccessToken(response);
			if (accessToken == null) {
				return;
			}

			redisTemplate.opsForValue()
				.set(IP_KEY + requestId, accessToken, expiration, TimeUnit.MILLISECONDS);
		}));
	}

	private String extractAccessToken(ServerHttpResponse response) {
		// 쿠키에서 Authorization 헤더 확인
		List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
		if (cookies != null) {
			for (String cookie : cookies) {
				if (cookie.startsWith("ACCESS_TOKEN=")) {
					return cookie.split(";")[0].substring(13);
				}
			}
		}
		return null;
	}

	@Override
	public int getOrder() {
		return 2;
	}
}
