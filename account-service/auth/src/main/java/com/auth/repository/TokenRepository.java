package com.auth.repository;

public interface TokenRepository {
	void save(String userId, String token);

	String findByUserId(String userId);

	void deleteByUserId(String userId);
}
