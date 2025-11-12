package com.user.infrastructure.jpa.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.UserExceptionCode;
import com.user.application.port.out.UserPersistencePort;
import com.user.domain.model.User;
import com.user.infrastructure.jpa.entity.UserEntity;
import com.user.infrastructure.jpa.repository.UserJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Repository
public class UserPersistenceAdapter implements UserPersistencePort {

	private final UserJpaRepository userJpaRepository;

	@Override
	public User save(com.user.domain.model.User user) {

		UserEntity entity =UserEntityMapper.toEntity(
			user);
		UserEntity userEntity = userJpaRepository.save(entity);

		return UserEntityMapper.toDomain(userEntity);
	}

	@Transactional(readOnly = true)
	@Override
	public User findById(String id) {
		UserEntity userEntity = getUserEntity(id);
		return UserEntityMapper.toDomain(userEntity);
	}

	@Override
	public void update(User user) {
		UserEntity userEntity = getUserEntity(user.getId());

		userEntity.updateNickname(user.getNickname());
		userEntity.updatePhoneNumber(user.getPhoneNumber());
		userEntity.updateAddress(
			UserEntityMapper.toEmbeddedAddress(user.getAddress()));
	}

	@Override
	public void withdraw(String id) {
		UserEntity userEntity = getUserEntity(id);
		userEntity.delete();
	}

	@Override
	public void delete(String id) {

	}

	@Override
	public boolean existsPhoneNumber(String phoneNumber) {
		return userJpaRepository.existsUserEntitiesByPhoneNumber(phoneNumber);
	}

	@Override
	public boolean existsNickname(String nickname) {
		return userJpaRepository.existsUserEntitiesByNickname(nickname);
	}

	@Override
	public void updateRole(String id, com.user.domain.vo.UserRole userRole) {
		UserEntity userEntity = getUserEntity(id);
		userEntity.getRoles().add(userRole);
	}

	@Override
	public void updateImage(String userId, String imageId, String profileImageUrl) {
		UserEntity userEntity = getUserEntity(userId);
		userEntity.changeProfileImage(imageId, profileImageUrl);
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<User> findByEmailAndOAuthId(String email, String oAuthId) {

		return userJpaRepository.findByEmailAndOauthId(email, oAuthId)
			.map(UserEntityMapper::toDomain);
	}

	private UserEntity getUserEntity(String userId) {
		return userJpaRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND.getMessage()));
	}
}

