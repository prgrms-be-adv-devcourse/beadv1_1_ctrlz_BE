package com.gatewayservice.filter;

import static com.gatewayservice.common.JwtClaimsConst.*;
import static com.gatewayservice.common.ServeletConst.*;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatewayservice.dto.TokenAuthorizationResponse;
import com.gatewayservice.handler.UserVerificationHandler;
import com.gatewayservice.utils.ServletRequestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	@Value("${jwt.secret}")
	private String secretKey;

	private final ObjectMapper objectMapper;
	private final UserVerificationHandler userVerificationHandler;

	@Data
	public static class Config {
		private String requiredRole;
	}

	public AuthenticationFilter(UserVerificationHandler userVerificationHandler) {
		super(Config.class);
		this.objectMapper = new ObjectMapper();
		this.userVerificationHandler = userVerificationHandler;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {

			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();

			Optional<String> tokenOptional = ServletRequestUtils.resolveToken(request);
			if (tokenOptional.isEmpty()) {
				log.info("Authorization token not found in header or cookie");
				return response.writeWith(
					Flux.just(writeUnAuthorizationResponseBody(response))
				);
			}
			String token = tokenOptional.get();

			try {
				if (!isValidToken(token)) {
					return response.writeWith(
						Flux.just(writeUnAuthorizationResponseBody(response))
					);
				}
			} catch (ExpiredJwtException e) {
				return response.writeWith(
					Flux.just(writeJwtExpiredResponseBody(response))
				);
			}

			boolean isInvalid
				= userVerificationHandler.verifyTokenWithIp(ServletRequestUtils.extractIp(request), token);
			if (isInvalid) {
				log.info("accessToken과 ip가 일치하지 않습니다.");
				return response.writeWith(
					Flux.just(writeUnAuthorizationResponseBody(response))
				);
			}

			Jws<Claims> claims = getClaims(token);

			List<Object> roles = claims.getPayload().get(ROLES, List.class);

			if (roles.isEmpty()) {
				throw new JwtException("권한이 없습니다.");
			}

			List<String> list = roles.stream().map(Object::toString).toList();

			if (list.isEmpty() || !list.contains(config.getRequiredRole())) {
				return response.writeWith(
					Flux.just(writeForbiddenResponseBody(response))
				);
			}

			String userId = claims.getPayload().get(USER_ID).toString();
			ServerHttpRequest authorizedRequest = request.mutate()
				.header(X_REQUEST_ID, userId)
				.build();

			log.warn("AuthenticationFilter userId = {}", userId);

			return chain.filter(exchange.mutate().request(authorizedRequest).build());
		};
	}

	private byte[] writeResponseBody(TokenAuthorizationResponse body) {
		try {
			return objectMapper.writeValueAsBytes(body);
		} catch (JsonProcessingException e) {
			log.info("JsonProcessingException e : {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private DataBuffer writeJwtExpiredResponseBody(ServerHttpResponse response) {
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.setRawStatusCode(456);
		response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		TokenAuthorizationResponse body = new TokenAuthorizationResponse("토큰 유효시간이 만료되었습니다.");
		return response.bufferFactory().wrap(writeResponseBody(body));
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
		} catch (ExpiredJwtException e) {
			log.info("토큰 시간 만료. : {}", e.getMessage());
			throw e;
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
