package com.userservice.application.port.out;

import com.userservice.domain.model.User;

public interface UserPersistencePort {
	User findById(String id);
	User save(User user);
	void update(User user);
	User findByEmail(String email);
	User findBynickname(String nickname);
	void withdraw(String id);
	boolean existsPhoneNumber(String phoneNumber);
	boolean existsNickname(String nickname);
}
