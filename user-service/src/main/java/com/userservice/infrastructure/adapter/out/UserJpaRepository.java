package com.userservice.infrastructure.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;

import com.userservice.infrastructure.model.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
	boolean existByPhoneNumber(String phoneNumber);
	boolean existsByNickname(String nickname);
}
