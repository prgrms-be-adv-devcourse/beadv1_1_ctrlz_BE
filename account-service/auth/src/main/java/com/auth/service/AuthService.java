package com.auth.service;

import com.auth.dto.LoginRequest;
import com.auth.dto.LoginResponse;

public interface AuthService {
	void saveRefreshToken(String userId, String refreshToken);
	String reissueAccessToken(String userId, String refreshToken);
	void logout(String userId);
	LoginResponse processLogin(LoginRequest request);
}
