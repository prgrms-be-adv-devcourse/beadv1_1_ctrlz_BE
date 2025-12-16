package com.auth.adapter;

import com.auth.handler.CookieProvider;
import com.auth.jwt.JwtTokenProvider;
import com.auth.jwt.TokenType;
import com.user.domain.vo.UserRole;
import com.user.infrastructure.reader.port.TokenWriterPort;
import com.user.infrastructure.reader.port.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthAdapter implements TokenWriterPort {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public TokenResponse issueUserRoleToken(String userId) {
		String accessToken = jwtTokenProvider.createAccessToken(userId, List.of(UserRole.USER));
		String refreshToken = jwtTokenProvider.createRefreshToken(userId, List.of(UserRole.USER));

		ResponseCookie accessTokenCookie =
			CookieProvider.to(
				TokenType.ACCESS_TOKEN.name(),
				accessToken,
				TokenType.ACCESS_TOKEN.getDuration()
			);

		ResponseCookie refreshTokenCookie =
			CookieProvider.to(TokenType.REFRESH_TOKEN.name(),
				refreshToken,
				TokenType.REFRESH_TOKEN.getDuration()
			);

		return new TokenResponse(accessTokenCookie, refreshTokenCookie);
	}

	@Override
	public TokenResponse issueSellerRoleToken(String userId) {
		String accessToken = jwtTokenProvider.createAccessToken(userId, List.of(UserRole.USER, UserRole.SELLER));
		String refreshToken = jwtTokenProvider.createRefreshToken(userId, List.of(UserRole.USER, UserRole.SELLER));

		ResponseCookie accessTokenCookie =
				CookieProvider.to(
						TokenType.ACCESS_TOKEN.name(),
						accessToken,
						TokenType.ACCESS_TOKEN.getDuration()
				);

		ResponseCookie refreshTokenCookie =
				CookieProvider.to(TokenType.REFRESH_TOKEN.name(),
						refreshToken,
						TokenType.REFRESH_TOKEN.getDuration()
				);

		return new TokenResponse(accessTokenCookie, refreshTokenCookie);
	}
}
