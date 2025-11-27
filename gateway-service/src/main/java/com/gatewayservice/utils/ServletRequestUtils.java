package com.gatewayservice.utils;

import static java.util.Optional.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ServletRequestUtils {

	public static String extractIp(ServerHttpRequest request) {
		// X-Forwarded-For 헤더 확인 (프록시/로드밸런서 뒤에 있을 경우)
		String xff = request.getHeaders().getFirst("X-Real-IP");
		if (xff != null && !xff.isEmpty()) {
			return xff.split(",")[0].trim();
		}

		InetSocketAddress remoteAddress = request.getRemoteAddress();
		return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
	}

	public static Optional<String> resolveToken(ServerHttpRequest request) {

		String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			log.info("bearerToken.substring(7) = {}", bearerToken.substring(7));
			return of(bearerToken.replace("Bearer ", ""));
		}

		HttpCookie tokenCookie = request.getCookies().getFirst("accessToken");
		if (tokenCookie != null) {
			log.info("Optional.of(tokenCookie.getValue()) = {}", of(tokenCookie.getValue()));
			return of(tokenCookie.getValue());
		}

		return Optional.empty();
	}


	public static String extractAccessToken(ServerHttpResponse response) {
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


}
