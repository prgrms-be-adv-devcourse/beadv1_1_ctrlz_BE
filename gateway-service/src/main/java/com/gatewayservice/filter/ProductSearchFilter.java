package com.gatewayservice.filter;

import static java.util.Optional.*;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductSearchFilter extends AbstractGatewayFilterFactory<ProductSearchFilter.Config> {

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
				Jws<Claims> claims = getClaims(tokenOptional.get());
				String userId = claims.getPayload().get("userId").toString();
				ServerHttpRequest authorizedRequest = request.mutate()
					.header("X-REQUEST-ID", userId)
					.build();

				log.info("AuthenticationFilter userId = {}", userId);
				return chain.filter(exchange.mutate().request(authorizedRequest).build());
			}

			log.info("ProductSearchFilter anonymous");
			return chain.filter(exchange);
		};
	}

	private Optional<String> resolveToken(ServerHttpRequest request) {

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

	private Jws<Claims> getClaims(String token) {
		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
			.build()
			.parseSignedClaims(token);
	}
}
