package com.auth.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import com.auth.jwt.JwtTokenProvider;
import com.auth.repository.LogoutRedisDao;
import com.auth.repository.TokenRepository;
import com.user.domain.vo.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 서비스
 * - 토큰 갱신 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthService implements AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final TokenRepository refreshTokenRedisRepository;
	private final LogoutRedisDao logoutRedisDao;

	@Override
	public void saveRefreshToken(String userId, String refreshToken) {
		refreshTokenRedisRepository.save(userId, refreshToken);
		log.info("Refresh Token 저장 완료 - userId: {}", userId);
	}

	@Override
	public String reissueAccessToken(String userId, String refreshToken) {

		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new AuthorizationDeniedException("재 로그인이 필요합니다.");
		}

		String findRefreshToken = refreshTokenRedisRepository.findByUserId(userId);

		if (!findRefreshToken.equals(refreshToken)) {
			refreshTokenRedisRepository.deleteByUserId(userId);
			throw new AuthorizationDeniedException("재 로그인이 필요합니다.");
		}

		String provider = jwtTokenProvider.getProviderFromToken(findRefreshToken);
		List<UserRole> roles = jwtTokenProvider.getRolesFromToken(findRefreshToken);

		String newAccessToken = jwtTokenProvider.createAccessToken(userId, roles, provider);

		log.info("토큰 갱신 완료 - userId: {}", userId);

		return newAccessToken;
	}

	@Override
	public void logout(String userId, String accessToken) {
		Instant expiration = jwtTokenProvider.getExpirationFromToken(accessToken);
		long expireTime = Date.from(expiration).getTime();

		logoutRedisDao.addLogoutList(accessToken, expireTime);
		refreshTokenRedisRepository.deleteByUserId(userId);
		log.info("로그아웃 완료 - userId: {}", userId);
	}

	public boolean exists(String token) {
		String findToken = logoutRedisDao.findByToken(token);
		return findToken != null && !findToken.isBlank();
	}
}
