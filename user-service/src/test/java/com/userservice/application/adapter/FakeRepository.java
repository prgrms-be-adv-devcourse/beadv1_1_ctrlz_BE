package com.userservice.application.adapter;

import com.userservice.application.port.out.UserPersistencePort;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.UserRole;

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
	public User findByEmail(String email) {
		return null;
	}

	@Override
	public User findBynickname(String nickname) {
		return null;
	}

	@Override
	public void withdraw(String id) {

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
	public void delete(String id) {

	}

	@Override
	public void updateRole(String id, UserRole userRole) {

	}
}
