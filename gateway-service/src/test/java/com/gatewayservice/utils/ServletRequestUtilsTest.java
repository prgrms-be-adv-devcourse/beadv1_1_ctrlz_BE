package com.gatewayservice.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.net.InetSocketAddress;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServletRequestUtils 테스트")
class ServletRequestUtilsTest {

    @Test
    @DisplayName("X-Real-IP 헤더에서 IP 추출")
    void extractIp_FromXRealIpHeader() {
        // given
        String expectedIp = "192.168.1.100";
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header("X-Real-IP", expectedIp)
                .build();

        // when
        String actualIp = ServletRequestUtils.extractIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("X-Real-IP에 여러 IP가 있을 때 첫 번째 IP 추출")
    void extractIp_FromXRealIpHeaderWithMultipleIps() {
        // given
        String expectedIp = "192.168.1.100";
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header("X-Real-IP", expectedIp + ", 10.0.0.1, 172.16.0.1")
                .build();

        // when
        String actualIp = ServletRequestUtils.extractIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("X-Real-IP가 없을 때 RemoteAddress에서 IP 추출")
    void extractIp_FromRemoteAddress() {
        // given
        String expectedIp = "127.0.0.1";
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .remoteAddress(InetSocketAddress.createUnresolved(expectedIp, 8080))
                .build();

        // when
        String actualIp = ServletRequestUtils.extractIp(request);

        // then
        assertThat(actualIp).isEqualTo(expectedIp);
    }

    @Test
    @DisplayName("IP를 알 수 없을 때 'unknown' 반환")
    void extractIp_WhenNoIpAvailable_ShouldReturnUnknown() {
        // given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .build();

        // when
        String actualIp = ServletRequestUtils.extractIp(request);

        // then
        assertThat(actualIp).isEqualTo("unknown");
    }

    @Test
    @DisplayName("Authorization 헤더에서 Bearer 토큰 추출")
    void resolveToken_FromAuthorizationHeader() {
        // given
        String expectedToken = "test.jwt.token";
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expectedToken)
                .build();

        // when
        Optional<String> actualToken = ServletRequestUtils.resolveToken(request);

        // then
        assertThat(actualToken).isPresent();
        assertThat(actualToken.get()).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("쿠키에서 accessToken 추출")
    void resolveToken_FromCookie() {
        // given
        String expectedToken = "test.jwt.token";
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .cookie(ResponseCookie.from("accessToken", expectedToken).build())
                .build();

        // when
        Optional<String> actualToken = ServletRequestUtils.resolveToken(request);

        // then
        assertThat(actualToken).isPresent();
        assertThat(actualToken.get()).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("Authorization 헤더가 우선순위를 가짐")
    void resolveToken_HeaderTakesPrecedenceOverCookie() {
        // given
        String headerToken = "header.token";
        String cookieToken = "cookie.token";
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
                .cookie(ResponseCookie.from("accessToken", cookieToken).build())
                .build();

        // when
        Optional<String> actualToken = ServletRequestUtils.resolveToken(request);

        // then
        assertThat(actualToken).isPresent();
        assertThat(actualToken.get()).isEqualTo(headerToken);
    }

    @Test
    @DisplayName("Bearer 접두사 없는 Authorization 헤더는 무시됨")
    void resolveToken_WithoutBearerPrefix_ShouldReturnEmpty() {
        // given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header(HttpHeaders.AUTHORIZATION, "test.jwt.token")
                .build();

        // when
        Optional<String> actualToken = ServletRequestUtils.resolveToken(request);

        // then
        assertThat(actualToken).isEmpty();
    }

    @Test
    @DisplayName("토큰이 없을 때 빈 Optional 반환")
    void resolveToken_WhenNoToken_ShouldReturnEmpty() {
        // given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .build();

        // when
        Optional<String> actualToken = ServletRequestUtils.resolveToken(request);

        // then
        assertThat(actualToken).isEmpty();
    }

    @Test
    @DisplayName("응답의 Set-Cookie 헤더에서 ACCESS_TOKEN 추출")
    void extractAccessToken_FromSetCookieHeader() {
        // given
        String expectedToken = "test.access.token";
        MockServerHttpResponse response = new MockServerHttpResponse();
        response.getHeaders().add(HttpHeaders.SET_COOKIE, "ACCESS_TOKEN=" + expectedToken + "; Path=/; HttpOnly");

        // when
        String actualToken = ServletRequestUtils.extractAccessToken(response);

        // then
        assertThat(actualToken).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("Set-Cookie 헤더에 ACCESS_TOKEN이 없을 때 null 반환")
    void extractAccessToken_WhenNoAccessToken_ShouldReturnNull() {
        // given
        MockServerHttpResponse response = new MockServerHttpResponse();
        response.getHeaders().add(HttpHeaders.SET_COOKIE, "OTHER_COOKIE=value; Path=/");

        // when
        String actualToken = ServletRequestUtils.extractAccessToken(response);

        // then
        assertThat(actualToken).isNull();
    }

    @Test
    @DisplayName("Set-Cookie 헤더가 없을 때 null 반환")
    void extractAccessToken_WhenNoSetCookie_ShouldReturnNull() {
        // given
        MockServerHttpResponse response = new MockServerHttpResponse();

        // when
        String actualToken = ServletRequestUtils.extractAccessToken(response);

        // then
        assertThat(actualToken).isNull();
    }

    @Test
    @DisplayName("여러 Set-Cookie 중에서 ACCESS_TOKEN 찾기")
    void extractAccessToken_FromMultipleSetCookies() {
        // given
        String expectedToken = "test.access.token";
        MockServerHttpResponse response = new MockServerHttpResponse();
        response.getHeaders().add(HttpHeaders.SET_COOKIE, "SESSION=session123; Path=/");
        response.getHeaders().add(HttpHeaders.SET_COOKIE, "ACCESS_TOKEN=" + expectedToken + "; Path=/; HttpOnly");
        response.getHeaders().add(HttpHeaders.SET_COOKIE, "REFRESH_TOKEN=refresh123; Path=/");

        // when
        String actualToken = ServletRequestUtils.extractAccessToken(response);

        // then
        assertThat(actualToken).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("빈 Authorization 헤더는 무시됨")
    void resolveToken_WithEmptyAuthorizationHeader_ShouldReturnEmpty() {
        // given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header(HttpHeaders.AUTHORIZATION, "")
                .build();

        // when
        Optional<String> actualToken = ServletRequestUtils.resolveToken(request);

        // then
        assertThat(actualToken).isEmpty();
    }

    @Test
    @DisplayName("공백만 있는 Bearer 토큰은 빈 문자열로 추출")
    void resolveToken_WithEmptyBearerToken_ShouldReturnEmptyString() {
        // given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();

        // when
        Optional<String> actualToken = ServletRequestUtils.resolveToken(request);

        // then
        assertThat(actualToken).isPresent();
        assertThat(actualToken.get()).isEmpty();
    }
}
