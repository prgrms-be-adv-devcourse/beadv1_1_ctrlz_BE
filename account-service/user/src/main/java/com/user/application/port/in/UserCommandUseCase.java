package com.user.application.port.in;

import com.user.application.adapter.dto.UserContext;
import com.user.application.adapter.dto.UserUpdateContext;
import com.user.domain.model.User;

public interface UserCommandUseCase {

	User getUser(String id);
	UserContext create(UserContext userContext);
	void delete(String id);
	void updateForSeller(String id);
	void updateUser(String userId, UserUpdateContext userUpdateContext);
	void updateImage(String userId, String imageId, String profileImageUrl);
}
