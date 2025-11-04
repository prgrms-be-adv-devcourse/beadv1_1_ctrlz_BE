package com.userservice.infrastructure.adapter;

import org.springframework.stereotype.Repository;

import com.userservice.application.port.out.UserPersistencePort;
import com.userservice.domain.model.User;

@Repository
public class UserPersistenceAdapter implements UserPersistencePort {
	@Override
	public User findById(String id) {
		return null;
	}

	@Override
	public User save(User user) {
		return null;
	}

	@Override
	public User update(User user) {
		return null;
	}

	@Override
	public User findByEmail(String email) {
		return null;
	}

	@Override
	public User findBynickname(String nickname) {
		return null;
	}
}
