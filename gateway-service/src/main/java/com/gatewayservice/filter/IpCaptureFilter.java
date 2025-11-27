package com.gatewayservice.filter;

import java.net.InetSocketAddress;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class IpCaptureFilter extends AbstractGatewayFilterFactory<IpCaptureFilter.Config> implements Ordered {

	public IpCaptureFilter() {
		super(IpCaptureFilter.Config.class);
	}

	public static class Config {
	}

	@Override
	public GatewayFilter apply(IpCaptureFilter.Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			String userIp = extractIp(request);
			if (userIp == null) {
				return chain.filter(exchange);
			}
			// 요청 ID를 exchange에 저장하여 다음 필터에서 사용 가능하도록
			exchange.getAttributes().put("REQUEST_ID", userIp);
			return chain.filter(exchange);
		};
	}

	private String extractIp(ServerHttpRequest request) {
		// X-Forwarded-For 헤더 확인 (프록시/로드밸런서 뒤에 있을 경우)
		String xff = request.getHeaders().getFirst("X-Forwarded-For");
		if (xff != null && !xff.isEmpty()) {
			return xff.split(",")[0].trim();
		}

		InetSocketAddress remoteAddress = request.getRemoteAddress();
		return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
	}

	@Override
	public int getOrder() {
		return 1;
	}
}

