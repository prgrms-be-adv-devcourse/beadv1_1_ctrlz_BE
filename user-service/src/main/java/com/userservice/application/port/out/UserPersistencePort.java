package com.userservice.application.port.out;

import com.userservice.domain.model.User;
import com.userservice.domain.vo.UserRole;

public interface UserPersistencePort {
	User findById(String id);
	User save(User user);
	void update(User user);
	User findByEmail(String email);
	User findBynickname(String nickname);
	void withdraw(String id);
	boolean existsPhoneNumber(String phoneNumber);
	boolean existsNickname(String nickname);
	void delete(String id);
	void updateRole(String id, UserRole userRole);
	void updateImage(String userId, String imageId, String profileImageUrl);
}
