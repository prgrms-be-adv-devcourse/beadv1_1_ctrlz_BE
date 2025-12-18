package com.accountapplication.user.application.adapter;

import java.util.Optional;

import com.user.application.port.out.UserPersistencePort;
import com.user.domain.model.User;

public class FakeRepository implements UserPersistencePort {

	@Override
	public User findById(String id) {
		return null;
	}

	@Override
	public User save(User user) {
		return User.builder().id("test").build();
	}

	@Override
	public void update(User user) {

	}

	@Override
	public void withdraw(User user) {

	}

	@Override
	public void delete(String id) {

	}

	@Override
	public boolean existsPhoneNumber(String phoneNumber) {
		return true;
	}

	@Override
	public boolean existsNickname(String nickname) {
		return true;
	}

	@Override
	public void updateRolesForSeller(User user) {

	}

	@Override
	public void updateImage(User user) {

	}

	@Override
	public Optional<User> findByEmailAndOAuthId(String email, String oAuthId) {
		return Optional.empty();
	}
}
