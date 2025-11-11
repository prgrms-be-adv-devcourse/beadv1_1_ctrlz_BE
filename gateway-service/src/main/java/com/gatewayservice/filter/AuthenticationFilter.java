package com.gatewayservice.filter;

import static java.util.Optional.*;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	@Value("${jwt.secret}")
	private String secretKey;

	private final ObjectMapper objectMapper;

	@Data
	public static class Config {
		private String requiredRole;
	}

	public AuthenticationFilter() {
		super(Config.class);
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {

			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();

			if (!request.getHeaders().containsKey("Authorization")
				&& !request.getHeaders().containsKey("accessToken")
			) {
				log.info("Authorization header not found");
				return response.writeWith(
					Flux.just(writeUnAuthorizationResponseBody(response))
				);
			}

			Optional<String> tokenOptional = resolveToken(request);
			String token = tokenOptional.orElseThrow(() -> new JwtException("토큰이 필요합니다!"));

			if (!isValidToken(token)) {
				return response.writeWith(
					Flux.just(writeUnAuthorizationResponseBody(response))
				);
			}

			Jws<Claims> claims = getClaims(token);

			List<Object> roles = claims.getPayload().get("roles", List.class);

			if(roles.isEmpty()) {
				throw new JwtException("권한이 없습니다.");
			}

			List<String> list = roles.stream().map(Object::toString).toList();
			if (list.isEmpty() || !list.contains(config.getRequiredRole())) {
				return response.writeWith(
					Flux.just(writeForbiddenResponseBody(response))
				);
			}

			String userId = claims.getPayload().get("userId").toString();
			ServerHttpRequest authorizedRequest = request.mutate()
				.header("X-REQUEST-ID", userId)
				.build();

			log.warn("AuthenticationFilter userId = {}", userId);

			return chain.filter(exchange.mutate().request(authorizedRequest).build());
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

	private byte[] writeResponseBody(TokenAuthorizationResponse body) {
		try {
			return objectMapper.writeValueAsBytes(body);
		} catch (JsonProcessingException e) {
			log.info("JsonProcessingException e : {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private DataBuffer writeUnAuthorizationResponseBody(ServerHttpResponse response) {
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		TokenAuthorizationResponse body = new TokenAuthorizationResponse("인증이 필요합니다!");
		return response.bufferFactory().wrap(writeResponseBody(body));
	}

	private DataBuffer writeForbiddenResponseBody(ServerHttpResponse response) {
		response.setStatusCode(HttpStatus.FORBIDDEN);
		response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		TokenAuthorizationResponse body = new TokenAuthorizationResponse("접근 권한이 없습니다!");
		return response.bufferFactory().wrap(writeResponseBody(body));
	}

	private boolean isValidToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (JwtException e) {
			log.info("Invalid JWT Token was detected: {}  msg : {}", token, e.getMessage());
		} catch (IllegalArgumentException e) {
			log.info("JWT claims String is empty: {}  msg : {}", token, e.getMessage());
		} catch (Exception e) {
			log.error("an error raised from validating token : {}  msg : {}", token, e.getMessage());
		}

		return false;
	}

	private Jws<Claims> getClaims(String token) {
		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
			.build()
			.parseSignedClaims(token);
	}
}
