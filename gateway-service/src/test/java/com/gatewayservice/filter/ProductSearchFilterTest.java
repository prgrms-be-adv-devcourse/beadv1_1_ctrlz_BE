package com.gatewayservice.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchFilter 테스트")
class ProductSearchFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private ProductSearchFilter productSearchFilter;

    private static final String SECRET_KEY = "MySecretKeyForJWTTokenGenerationAndValidation1234567890";
    private static final String USER_ID = "testUser123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productSearchFilter, "secretKey", SECRET_KEY);
    }

    @Test
    @DisplayName("유효한 토큰이 있는 경우 X-REQUEST-ID 헤더 추가")
    void validTokenPresent_ShouldAddUserIdHeader() {
        // given
        String token = generateToken(USER_ID, List.of("USER"));
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        ProductSearchFilter.Config config = new ProductSearchFilter.Config();
        
        // Capture the modified exchange passed to filter chain
        when(filterChain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange modifiedExchange = invocation.getArgument(0);
            String userId = modifiedExchange.getRequest().getHeaders().getFirst("X-REQUEST-ID");
            assertThat(userId).isEqualTo(USER_ID);
            return Mono.empty();
        });

        // when
        GatewayFilter filter = productSearchFilter.apply(config);
        Mono<Void> result = filter.filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("토큰이 없는 경우 익명 사용자로 처리")
    void noToken_ShouldProcessAsAnonymous() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products/search")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        ProductSearchFilter.Config config = new ProductSearchFilter.Config();
        
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // when
        GatewayFilter filter = productSearchFilter.apply(config);
        Mono<Void> result = filter.filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
        assertThat(exchange.getRequest().getHeaders().getFirst("X-REQUEST-ID")).isNull();
    }

    @Test
    @DisplayName("쿠키에서 토큰을 추출하여 사용자 ID 헤더 추가")
    void validTokenFromCookie_ShouldAddUserIdHeader() {
        // given
        String token = generateToken(USER_ID, List.of("USER"));
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products/search")
                .cookie(org.springframework.http.ResponseCookie.from("accessToken", token).build())
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        ProductSearchFilter.Config config = new ProductSearchFilter.Config();
        
        // Capture the modified exchange passed to filter chain
        when(filterChain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange modifiedExchange = invocation.getArgument(0);
            String userId = modifiedExchange.getRequest().getHeaders().getFirst("X-REQUEST-ID");
            assertThat(userId).isEqualTo(USER_ID);
            return Mono.empty();
        });

        // when
        GatewayFilter filter = productSearchFilter.apply(config);
        Mono<Void> result = filter.filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("유효하지 않은 토큰인 경우 익명으로 처리")
    void invalidToken_ShouldProcessAsAnonymous() {
        // given
        String invalidToken = "invalid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        ProductSearchFilter.Config config = new ProductSearchFilter.Config();
        
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // when
        GatewayFilter filter = productSearchFilter.apply(config);
        Mono<Void> result = filter.filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
        assertThat(exchange.getRequest().getHeaders().getFirst("X-REQUEST-ID")).isNull();
    }

    @Test
    @DisplayName("Bearer 접두사 없는 토큰은 무시됨")
    void tokenWithoutBearerPrefix_ShouldProcessAsAnonymous() {
        // given
        String token = generateToken(USER_ID, List.of("USER"));
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products/search")
                .header(HttpHeaders.AUTHORIZATION, token)
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        ProductSearchFilter.Config config = new ProductSearchFilter.Config();
        
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // when
        GatewayFilter filter = productSearchFilter.apply(config);
        Mono<Void> result = filter.filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
        assertThat(exchange.getRequest().getHeaders().getFirst("X-REQUEST-ID")).isNull();
    }

    @Test
    @DisplayName("만료된 토큰인 경우 익명으로 처리")
    void expiredToken_ShouldProcessAsAnonymous() {
        // given
        String expiredToken = generateExpiredToken(USER_ID, List.of("USER"));
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        ProductSearchFilter.Config config = new ProductSearchFilter.Config();
        
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // when
        GatewayFilter filter = productSearchFilter.apply(config);
        Mono<Void> result = filter.filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
        assertThat(exchange.getRequest().getHeaders().getFirst("X-REQUEST-ID")).isNull();
    }

    private String generateToken(String userId, List<String> roles) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
    }

    private String generateExpiredToken(String userId, List<String> roles) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
    }
}
