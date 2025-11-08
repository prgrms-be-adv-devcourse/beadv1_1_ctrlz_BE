package com.auth.service;

public interface AuthService {
	void saveRefreshToken(String userId, String refreshToken);
	String reissueAccessToken(String userId, String refreshToken);
	void logout(String userId, String accessToken);
}
