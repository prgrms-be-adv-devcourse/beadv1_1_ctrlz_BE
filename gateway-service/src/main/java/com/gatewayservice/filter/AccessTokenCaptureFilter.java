package com.gatewayservice.filter;

import static com.gatewayservice.common.ServeletConst.*;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import com.gatewayservice.handler.UserVerificationHandler;
import com.gatewayservice.utils.ServletRequestUtils;

import reactor.core.publisher.Mono;

@Component
public class AccessTokenCaptureFilter
	extends AbstractGatewayFilterFactory<AccessTokenCaptureFilter.Config>
	implements Ordered {

	private final UserVerificationHandler userVerificationHandler;

	public AccessTokenCaptureFilter(UserVerificationHandler userVerificationHandler) {
		super(AccessTokenCaptureFilter.Config.class);
		this.userVerificationHandler = userVerificationHandler;
	}

	public static class Config {
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
			String requestIp = exchange.getAttribute(REQUEST_IP);
			if (requestIp == null) {
				return;
			}

			ServerHttpResponse response = exchange.getResponse();
			String accessToken = ServletRequestUtils.extractAccessToken(response);
			if (accessToken == null) {
				return;
			}

			userVerificationHandler.addTokenAndIp(accessToken, requestIp);
		}));
	}


	@Override
	public int getOrder() {
		return 2;
	}
}
