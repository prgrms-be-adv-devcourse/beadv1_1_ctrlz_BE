package com.userservice.application.port.in;

import com.userservice.application.adapter.dto.UserContext;
import com.userservice.domain.model.User;

public interface UserCommandUseCase {

	User getUser(String id);
	User create(UserContext userContext);
	void update(UserContext userContext);
	void delete(String id);
	void updateForSeller(String id);
}
