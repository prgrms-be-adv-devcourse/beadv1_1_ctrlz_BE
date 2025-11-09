package com.userservice.infrastructure.jpa.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.UserExceptionCode;
import com.userservice.application.port.out.UserPersistencePort;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.UserRole;
import com.userservice.infrastructure.jpa.entity.UserEntity;
import com.userservice.infrastructure.jpa.repository.UserJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Repository
public class UserPersistenceAdapter implements UserPersistencePort {

	private final UserJpaRepository userJpaRepository;

	@Override
	public User save(User user) {

		UserEntity entity = UserEntityMapper.toEntity(user);
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
		userEntity.updateAddress(UserEntityMapper.toEmbeddedAddress(user.getAddress()));
	}

	@Override
	public User findByEmail(String email) {
		return null;
	}

	@Override
	public User findBynickname(String nickname) {
		return null;
	}

	@Override
	public void withdraw(String id) {

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
	public void delete(String id) {
		userJpaRepository.deleteById(id);
	}

	@Override
	public void updateRole(String id, UserRole userRole) {
		UserEntity userEntity = getUserEntity(id);
		userEntity.getRoles().add(userRole);
	}

	@Override
	public void updateImage(String userId, String imageId, String profileImageUrl) {
		UserEntity userEntity = getUserEntity(userId);
		userEntity.changeProfileImage(imageId, profileImageUrl);
	}

	private UserEntity getUserEntity(String userId) {
		return userJpaRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND.getMessage()));
	}
}
