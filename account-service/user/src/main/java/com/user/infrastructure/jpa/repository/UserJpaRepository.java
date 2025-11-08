package com.user.infrastructure.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.infrastructure.jpa.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
	boolean existsUserEntitiesByPhoneNumber(String phoneNumber);
	boolean existsUserEntitiesByNickname(String nickname);
	
	Optional<UserEntity> findByEmailAndOauthId(String email, String oauthId);
	
	/**
	 * 이메일로 사용자 존재 여부 확인
	 * @param email 이메일
	 * @return 존재 여부
	 */
	boolean existsByEmail(String email);
}
