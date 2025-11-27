package com.gatewayservice.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatewayservice.handler.UserVerificationHandler;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

	@Mock
	private UserVerificationHandler userVerificationHandler;

	@Mock
	private GatewayFilterChain filterChain;

	@InjectMocks
	private AuthenticationFilter authenticationFilter;

	private static final String SECRET_KEY = "dfkdngppendnfopdoskenfkdkskdnfldkmdkn1234567890";
	private static final String USER_ID = "testUser";
	private static final String TEST_IP = "127.0.0.1";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(authenticationFilter, "secretKey", SECRET_KEY);
		ReflectionTestUtils.setField(authenticationFilter, "objectMapper", new ObjectMapper());
	}

	@DisplayName("유효한 토큰과 권한으로 필터 통과")
	@Test
	void test1() {
		// given
		String token = generateToken(USER_ID, List.of("USER", "ADMIN"));
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/test")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
			.header("X-Real-IP", TEST_IP)
			.build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		AuthenticationFilter.Config config = new AuthenticationFilter.Config();
		config.setRequiredRole("USER");

		when(userVerificationHandler.validateToken(anyString(), anyString())).thenReturn(false);
		
		// Capture the modified exchange passed to filter chain
		when(filterChain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
			ServerWebExchange modifiedExchange = invocation.getArgument(0);
			String userId = modifiedExchange.getRequest().getHeaders().getFirst("X-REQUEST-ID");
			assertThat(userId).isEqualTo(USER_ID);
			return Mono.empty();
		});

		// when
		GatewayFilter filter = authenticationFilter.apply(config);
		Mono<Void> result = filter.filter(exchange, filterChain);

		// then
		StepVerifier.create(result)
			.verifyComplete();

		verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
	}

	@Test
	@DisplayName("Authorization 헤더 없이 요청 시 401 반환")
	void noAuthorizationHeader_ShouldReturn401() {
		// given
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/test")
			.build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		AuthenticationFilter.Config config = new AuthenticationFilter.Config();
		config.setRequiredRole("USER");

		// when
		GatewayFilter filter = authenticationFilter.apply(config);
		Mono<Void> result = filter.filter(exchange, filterChain);

		// then
		StepVerifier.create(result)
			.verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(filterChain, never()).filter(any(ServerWebExchange.class));
	}

	@Test
	@DisplayName("유효하지 않은 토큰으로 요청 시 401 반환")
	void invalidToken_ShouldReturn401() {
		// given
		String invalidToken = "invalid.jwt.token";
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/test")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
			.remoteAddress(InetSocketAddress.createUnresolved(TEST_IP, 8080))
			.build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		AuthenticationFilter.Config config = new AuthenticationFilter.Config();
		config.setRequiredRole("USER");

		// when
		GatewayFilter filter = authenticationFilter.apply(config);
		Mono<Void> result = filter.filter(exchange, filterChain);

		// then
		StepVerifier.create(result)
			.verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(filterChain, never()).filter(any(ServerWebExchange.class));
	}

	@Test
	@DisplayName("필요한 권한이 없는 경우 403 반환")
	void insufficientRole_ShouldReturn403() {
		// given
		String token = generateToken(USER_ID, List.of("USER"));
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/test")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
			.remoteAddress(InetSocketAddress.createUnresolved(TEST_IP, 8080))
			.build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		AuthenticationFilter.Config config = new AuthenticationFilter.Config();
		config.setRequiredRole("ADMIN");

		when(userVerificationHandler.validateToken(anyString(), anyString())).thenReturn(false);

		// when
		GatewayFilter filter = authenticationFilter.apply(config);
		Mono<Void> result = filter.filter(exchange, filterChain);

		// then
		StepVerifier.create(result)
			.verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		verify(filterChain, never()).filter(any(ServerWebExchange.class));
	}

	@Test
	@DisplayName("UserVerificationHandler 검증 실패 시 401 반환")
	void userVerificationFailed_ShouldReturn401() {
		// given
		String token = generateToken(USER_ID, List.of("USER"));
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/test")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
			.remoteAddress(InetSocketAddress.createUnresolved(TEST_IP, 8080))
			.build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		AuthenticationFilter.Config config = new AuthenticationFilter.Config();
		config.setRequiredRole("USER");

		when(userVerificationHandler.validateToken(anyString(), anyString())).thenReturn(true);

		// when
		GatewayFilter filter = authenticationFilter.apply(config);
		Mono<Void> result = filter.filter(exchange, filterChain);

		// then
		StepVerifier.create(result)
			.verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(filterChain, never()).filter(any(ServerWebExchange.class));
	}

	@Test
	@DisplayName("쿠키에서 토큰을 추출하여 인증 성공")
	void validTokenFromCookie_ShouldPassFilter() {
		// given
		String token = generateToken(USER_ID, List.of("USER"));
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/test")
			.cookie(org.springframework.http.ResponseCookie.from("accessToken", token).build())
			.remoteAddress(InetSocketAddress.createUnresolved(TEST_IP, 8080))
			.build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		AuthenticationFilter.Config config = new AuthenticationFilter.Config();
		config.setRequiredRole("USER");

		when(userVerificationHandler.validateToken(anyString(), anyString())).thenReturn(false);
		when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// when
		GatewayFilter filter = authenticationFilter.apply(config);
		Mono<Void> result = filter.filter(exchange, filterChain);

		// then
		StepVerifier.create(result)
			.verifyComplete();

		verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
	}

	@Test
	@DisplayName("만료된 토큰으로 요청 시 401 반환")
	void expiredToken_ShouldReturn401() {
		// given
		String expiredToken = generateExpiredToken(USER_ID, List.of("USER"));
		MockServerHttpRequest request = MockServerHttpRequest
			.get("/api/test")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
			.remoteAddress(InetSocketAddress.createUnresolved(TEST_IP, 8080))
			.build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		AuthenticationFilter.Config config = new AuthenticationFilter.Config();
		config.setRequiredRole("USER");

		// when
		GatewayFilter filter = authenticationFilter.apply(config);
		Mono<Void> result = filter.filter(exchange, filterChain);

		// then
		StepVerifier.create(result)
			.verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(filterChain, never()).filter(any(ServerWebExchange.class));
	}

	private String generateToken(String userId, List<String> roles) {
		return Jwts.builder()
			.claim("userId", userId)
			.claim("roles", roles)
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + 360000))
			.signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
			.compact();
	}

	private String generateExpiredToken(String userId, List<String> roles) {
		return Jwts.builder()
			.claim("userId", userId)
			.claim("roles", roles)
			.issuedAt(new Date(System.currentTimeMillis() - 720000))
			.expiration(new Date(System.currentTimeMillis() - 360000))
			.signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
			.compact();
	}
}
