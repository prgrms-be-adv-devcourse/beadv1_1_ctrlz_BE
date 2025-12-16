package com.gatewayservice.filter;

import static java.util.Optional.*;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.gatewayservice.common.ServletConst;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class ProductSearchFilter extends AbstractGatewayFilterFactory<ProductSearchFilter.Config> {

	private static final Logger search_logger = LoggerFactory.getLogger("SEARCH_VIEW");
	private static final Logger log = LoggerFactory.getLogger("API." + ProductSearchFilter.class);
	@Value("${jwt.secret}")
	private String secretKey;

	public static class Config {
	}

	public ProductSearchFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			Optional<String> tokenOptional = resolveToken(request);


			if (tokenOptional.isPresent()) {
				try {
					Jws<Claims> claims = getClaims(tokenOptional.get());
					String userId = claims.getPayload().get("userId").toString();
					ServerHttpRequest authorizedRequest = request.mutate()
						.header("X-REQUEST-ID", userId)
						.build();
					log.info("ProductSearchFilter: User identified: {}", userId);
					searchInfoLogging(request, userId);
					return chain.filter(exchange.mutate().request(authorizedRequest).build());
				} catch (Exception e) {
					log.warn("ProductSearchFilter: Token validation failed. Proceeding as anonymous.", e);
				}
			}

			log.info("ProductSearchFilter: No token found. Proceeding as anonymous.");
			return chain.filter(exchange);
		};
	}

	private void searchInfoLogging(ServerHttpRequest request, String userId) {
		List<String> q = request.getQueryParams().get("q");
		if(q == null || q.isEmpty()) {
			return;
		}
		String query = q.getFirst();
		search_logger.info("query = {}, userId = {}", query, userId);
	}

	private Optional<String> resolveToken(ServerHttpRequest request) {

		String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			log.info("bearerToken.substring(7) = {}", bearerToken.substring(7));
			return of(bearerToken.replace("Bearer ", ""));
		}

		HttpCookie tokenCookie = request.getCookies().getFirst(ServletConst.ACCESS_TOKEN);
		if (tokenCookie != null) {
			log.info("Optional.of(tokenCookie.getValue()) = {}", of(tokenCookie.getValue()));
			return of(tokenCookie.getValue());
		}

		return Optional.empty();
	}

	private Jws<Claims> getClaims(String token) {
		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
			.build()
			.parseSignedClaims(token);
	}
}
