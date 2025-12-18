package com.user.application.port.out;

import java.util.Optional;

import com.user.domain.model.User;

public interface UserPersistencePort {

	User findById(String id);

	User save(User user);

	void update(User user);

	void withdraw(User user);

	void delete(String id);
	
	boolean existsPhoneNumber(String phoneNumber);

	boolean existsNickname(String nickname);

	void updateRolesForSeller(User user);

	void updateImage(User user);

	Optional<User> findByEmailAndOAuthId(String email, String oAuthId);

}
