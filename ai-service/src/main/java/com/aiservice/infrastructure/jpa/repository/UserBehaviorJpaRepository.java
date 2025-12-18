package com.aiservice.infrastructure.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiservice.domain.model.UserBehavior;

public interface UserBehaviorJpaRepository extends JpaRepository<UserBehavior, Long> {
	List<UserBehavior> findByUserId(String userId);
}
