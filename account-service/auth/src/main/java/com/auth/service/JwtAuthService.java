package com.auth.service;

import java.util.List;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import com.auth.dto.LoginResponse;
import com.auth.dto.LoginRequest;
import com.auth.jwt.JwtTokenProvider;
import com.auth.repository.TokenRepository;
import com.user.application.port.out.UserPersistencePort;
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
	private final UserPersistencePort userPersistencePort;

	@Override
	public LoginResponse processLogin(LoginRequest request) {
		log.info("로그인 처리 시작: email={}", request.email());

		return userPersistencePort.findByEmailAndOAuthId(request.email(), request.provider())
			//기존 유저
			.map(user -> {
				String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRoles());
				String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRoles());
				saveRefreshToken(user.getId(), refreshToken);

				return LoginResponse.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.userId(user.getId())
					.email(user.getEmail())
					.nickname(user.getNickname())
					.profileImageUrl(user.getProfileImageUrl())
					.isNewUser(false)
					.build();
			})
			//신규 유저
			.orElseGet(() -> LoginResponse.builder()
				.email(request.email())
				.nickname(request.nickname())
				.profileImageUrl(request.profileImageUrl())
				.isNewUser(true)
				.build());
	}

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

		List<UserRole> roles = jwtTokenProvider.getRolesFromToken(findRefreshToken);

		String newAccessToken = jwtTokenProvider.createAccessToken(userId, roles);

		log.info("토큰 갱신 완료 - userId: {}", userId);

		return newAccessToken;
	}

	@Override
	public void logout(String userId) {
		refreshTokenRedisRepository.deleteByUserId(userId);
		log.info("로그아웃 완료 - userId: {}", userId);
	}
}
