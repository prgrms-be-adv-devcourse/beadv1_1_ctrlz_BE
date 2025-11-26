package com.auth.adapter;

import java.util.List;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.auth.handler.CookieProvider;
import com.auth.jwt.JwtTokenProvider;
import com.auth.jwt.TokenType;
import com.user.domain.vo.UserRole;
import com.user.infrastructure.reader.port.TokenWriterPort;
import com.user.infrastructure.reader.port.dto.TokenResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthAdapter implements TokenWriterPort {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public TokenResponse issueToken(String userId) {
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
}
