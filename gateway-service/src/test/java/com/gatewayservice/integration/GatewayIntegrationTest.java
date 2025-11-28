package com.gatewayservice.integration;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.gatewayservice.handler.UserVerificationHandler;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Gateway 통합 테스트
 * 
 * 주의: 이 테스트는 실제 서비스가 구동되어 있을 때 동작합니다.
 * Eureka, 백엔드 서비스들이 실행 중이어야 합니다.
 * 
 * 실제 환경 테스트가 필요할 때 @Disabled 어노테이션을 제거하고 실행하세요.
 */
@Disabled("실제 환경이 필요한 통합 테스트 - 수동으로만 실행")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DisplayName("Gateway 통합 테스트")
class GatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserVerificationHandler userVerificationHandler;

    @Value("${jwt.secret}")
    private String secretKey;

    private static final String USER_ID = "testUser123";
    private static final String TEST_IP = "127.0.0.1";

    @BeforeEach
    void setUp() {
        // 테스트용 타임아웃 설정
        webTestClient = webTestClient.mutate()
                .responseTimeout(java.time.Duration.ofSeconds(30))
                .build();
    }

    @Test
    @DisplayName("인증 없이 보호된 엔드포인트 접근 시 401 반환")
    void accessProtectedEndpoint_WithoutAuth_ShouldReturn401() {
        webTestClient.get()
                .uri("/api/protected")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("유효한 토큰으로 보호된 엔드포인트 접근 성공")
    void accessProtectedEndpoint_WithValidToken_ShouldSucceed() {
        // given
        String token = generateToken(USER_ID, List.of("USER"));
        userVerificationHandler.addTokenAndIp(token, TEST_IP);

        // when & then
        webTestClient.get()
                .uri("/api/user/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Real-IP", TEST_IP)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 접근 시 401 반환")
    void accessProtectedEndpoint_WithInvalidToken_ShouldReturn401() {
        webTestClient.get()
                .uri("/api/user/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("권한이 부족한 토큰으로 관리자 엔드포인트 접근 시 403 반환")
    void accessAdminEndpoint_WithUserRole_ShouldReturn403() {
        // given
        String token = generateToken(USER_ID, List.of("USER"));
        userVerificationHandler.addTokenAndIp(token, TEST_IP);

        // when & then
        webTestClient.get()
                .uri("/api/admin/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Real-IP", TEST_IP)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("ADMIN 권한으로 관리자 엔드포인트 접근 성공")
    void accessAdminEndpoint_WithAdminRole_ShouldSucceed() {
        // given
        String token = generateToken(USER_ID, List.of("USER", "ADMIN"));
        userVerificationHandler.addTokenAndIp(token, TEST_IP);

        // when & then
        webTestClient.get()
                .uri("/api/admin/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Real-IP", TEST_IP)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("토큰 없이 상품 검색 엔드포인트 접근 가능 (익명)")
    void accessProductSearch_WithoutToken_ShouldSucceed() {
        webTestClient.get()
                .uri("/api/products/search?keyword=test")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("토큰과 함께 상품 검색 시 사용자 ID 전달")
    void accessProductSearch_WithToken_ShouldIncludeUserId() {
        // given
        String token = generateToken(USER_ID, List.of("USER"));

        // when & then
        webTestClient.get()
                .uri("/api/products/search?keyword=test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("다른 IP에서 같은 토큰 사용 시 거부")
    void accessWithSameToken_FromDifferentIp_ShouldBeDenied() {
        // given
        String token = generateToken(USER_ID, List.of("USER"));
        userVerificationHandler.addTokenAndIp(token, TEST_IP);

        // when & then
        webTestClient.get()
                .uri("/api/user/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Real-IP", "192.168.1.100") // 다른 IP
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("쿠키를 통한 인증 처리")
    void accessWithCookie_ShouldAuthenticate() {
        // given
        String token = generateToken(USER_ID, List.of("USER"));
        userVerificationHandler.addTokenAndIp(token, TEST_IP);

        // when & then
        webTestClient.get()
                .uri("/api/user/profile")
                .cookie("accessToken", token)
                .header("X-Real-IP", TEST_IP)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("만료된 토큰으로 접근 시 401 반환")
    void accessWithExpiredToken_ShouldReturn401() {
        // given
        String expiredToken = generateExpiredToken(USER_ID, List.of("USER"));

        // when & then
        webTestClient.get()
                .uri("/api/user/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .header("X-Real-IP", TEST_IP)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private String generateToken(String userId, List<String> roles) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    private String generateExpiredToken(String userId, List<String> roles) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }
}
