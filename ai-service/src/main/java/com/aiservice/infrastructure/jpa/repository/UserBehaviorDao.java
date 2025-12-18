package com.aiservice.infrastructure.jpa.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.aiservice.domain.model.UserBehavior;
import com.aiservice.domain.repository.UserBehaviorRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class UserBehaviorDao implements UserBehaviorRepository {

	private final UserBehaviorJpaRepository userBehaviorJpaRepository;

	@Override
	public void save(UserBehavior userBehavior) {
		userBehaviorJpaRepository.save(userBehavior);
	}

	@Override
	public List<UserBehavior> findByUserId(String userId) {
		return userBehaviorJpaRepository.findByUserId(userId);
	}
}
