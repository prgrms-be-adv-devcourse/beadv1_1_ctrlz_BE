package com.gatewayservice.filter;

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
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class ReissueTokenFilter extends AbstractGatewayFilterFactory<ReissueTokenFilter.Config> {

	@Value("${jwt.secret}")
	private String secretKey;

	private final ObjectMapper objectMapper;
	private final UserVerificationHandler userVerificationHandler;

	@Data
	public static class Config {
		private String requiredRole;
	}

	public ReissueTokenFilter(UserVerificationHandler userVerificationHandler) {
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

			Jws<Claims> claims = getClaims(token);

			String userId = claims.getPayload().get("userId").toString();
			ServerHttpRequest authorizedRequest = request.mutate()
				.header("X-REQUEST-ID", userId)
				.build();

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
		response.setRawStatusCode(444);
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

	private Jws<Claims> getClaims(String token) {
		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
			.build()
			.parseSignedClaims(token);
	}

}
