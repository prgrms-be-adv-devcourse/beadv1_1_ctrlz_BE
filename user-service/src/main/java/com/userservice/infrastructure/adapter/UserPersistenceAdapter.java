package com.userservice.infrastructure.adapter.out;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Repository;

import com.userservice.application.port.out.UserPersistencePort;
import com.userservice.domain.model.User;
import com.userservice.infrastructure.model.entity.UserEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserPersistenceAdapter implements UserPersistencePort {

	private final UserJpaRepository userJpaRepository;

	@Override
	public User findById(String id) {
		UserEntity userEntity = userJpaRepository.findById(id).orElseThrow(NoSuchElementException::new);
		return UserEntityMapper.toDomain(userEntity);
	}

	@Override
	public User save(User user) {

		UserEntity entity = UserEntityMapper.toEntity(user);
		UserEntity userEntity = userJpaRepository.save(entity);

		return UserEntityMapper.toDomain(userEntity);
	}

	@Override
	public void update(User user) {
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
		return userJpaRepository.existByPhoneNumber(phoneNumber);
	}

	@Override
	public boolean existsNickname(String nickname) {
		return userJpaRepository.existsByNickname(nickname);
	}
}
