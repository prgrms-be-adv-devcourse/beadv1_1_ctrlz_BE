package com.auth.jwt;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.user.domain.vo.UserRole;

class JwtTokenProviderTest {

	private JwtTokenProvider jwtTokenProvider;

	private String userId;
	private List<UserRole> roles;
	private String provider;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider();
		ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "sdfsdfidijfidnfiesdfsdfsdfsdfsdfnsldkfneosdkjfnsdkdfsdfsdfsdfsdfldfmdkfm");
		ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 3600L);
		ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 86400L);
		jwtTokenProvider.init();

		userId = "testUser";
		roles = List.of(UserRole.USER, UserRole.SELLER);
		provider = "testProvider";
	}

	@Test
	@DisplayName("액세스 토큰 생성 테스트")
	void createAccessToken() {
		String accessToken = jwtTokenProvider.createAccessToken(userId, roles, provider);

		assertThat(accessToken).isNotNull();
		assertThat(jwtTokenProvider.getUserIdFromToken(accessToken)).isEqualTo(userId);
		assertThat(jwtTokenProvider.getRolesFromToken(accessToken)).isEqualTo(roles);
		assertThat(jwtTokenProvider.getProviderFromToken(accessToken)).isEqualTo(provider);
	}

	@Test
	@DisplayName("리프레시 토큰 생성 테스트")
	void createRefreshToken() {
		String refreshToken = jwtTokenProvider.createRefreshToken(userId, roles, provider);

		assertThat(refreshToken).isNotNull();
		assertThat(jwtTokenProvider.getUserIdFromToken(refreshToken)).isEqualTo(userId);
		assertThat(jwtTokenProvider.getRolesFromToken(refreshToken)).isEqualTo(roles);
		assertThat(jwtTokenProvider.getProviderFromToken(refreshToken)).isEqualTo(provider);
	}

	@Test
	@DisplayName("토큰에서 사용자 ID 추출 테스트")
	void getUserIdFromToken() {
		String accessToken = jwtTokenProvider.createAccessToken(userId, roles, provider);
		String extractedUserId = jwtTokenProvider.getUserIdFromToken(accessToken);

		assertThat(extractedUserId).isEqualTo(userId);
	}

	@Test
	@DisplayName("토큰에서 역할 추출 테스트")
	void getRolesFromToken() {
		String accessToken = jwtTokenProvider.createAccessToken(userId, roles, provider);
		List<UserRole> extractedRoles = jwtTokenProvider.getRolesFromToken(accessToken);

		assertThat(extractedRoles).isEqualTo(roles);
	}

	@Test
	@DisplayName("토큰에서 제공자 추출 테스트")
	void getProviderFromToken() {
		String accessToken = jwtTokenProvider.createAccessToken(userId, roles, provider);
		String extractedProvider = jwtTokenProvider.getProviderFromToken(accessToken);

		assertThat(extractedProvider).isEqualTo(provider);
	}

	@Test
	@DisplayName("유효한 토큰 검증 테스트")
	void validateToken_valid() {
		String accessToken = jwtTokenProvider.createAccessToken(userId, roles, provider);

		assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
	}

	@Test
	@DisplayName("만료된 토큰 검증 테스트")
	void validateToken_expired() {
		JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
		ReflectionTestUtils.setField(expiredTokenProvider, "secretKey", "super-secret-key-for-testing-1234567890-super-secret-key-for-testing-1234567890");
		ReflectionTestUtils.setField(expiredTokenProvider, "accessTokenExpiration", -1L);
		expiredTokenProvider.init();

		String expiredToken = expiredTokenProvider.createAccessToken(userId, roles, provider);

		assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
	}

	@Test
	@DisplayName("잘못된 서명 토큰 검증 테스트")
	void validateToken_invalidSignature() {
		String invalidSecret = "invalid-secret-key-for-testing-1234567890-invalid-secret-key-for-testing-1234567890";
		JwtTokenProvider anotherProvider = new JwtTokenProvider();
		ReflectionTestUtils.setField(anotherProvider, "secretKey", invalidSecret);
		ReflectionTestUtils.setField(anotherProvider, "accessTokenExpiration", 3600L);
		anotherProvider.init();

		String tokenWithInvalidSignature = anotherProvider.createAccessToken(userId, roles, provider);

		assertThat(jwtTokenProvider.validateToken(tokenWithInvalidSignature)).isFalse();
	}

	@Test
	@DisplayName("잘못된 형식의 토큰 검증 테스트")
	void validateToken_malformed() {
		String malformedToken = "this.is.not.a.jwt";

		assertThat(jwtTokenProvider.validateToken(malformedToken)).isFalse();
	}

	@Test
	@DisplayName("토큰 만료 시간 추출 테스트")
	void getExpirationFromToken() {
		String accessToken = jwtTokenProvider.createAccessToken(userId, roles, provider);
		Instant expiration = jwtTokenProvider.getExpirationFromToken(accessToken);

		assertThat(expiration).isAfter(Instant.now());
	}
}
