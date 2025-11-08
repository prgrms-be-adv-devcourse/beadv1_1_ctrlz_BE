package com.userservice.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.userservice.infrastructure.jpa.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
	boolean existsUserEntitiesByPhoneNumber(String phoneNumber);
	boolean existsUserEntitiesByNickname(String nickname);
}
