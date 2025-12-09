package com.aiservice.domain.repository;

import java.util.List;

import com.aiservice.domain.model.UserBehavior;

public interface UserBehaviorRepository {

	void save(UserBehavior userBehavior);

	List<UserBehavior> findByUserId(String userId);
}
