package com.gatewayservice.handler;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
	ACCESS_TOKEN(Duration.ofMinutes(15)), REFRESH_TOKEN(Duration.ofDays(7));

	private final Duration duration;
}
