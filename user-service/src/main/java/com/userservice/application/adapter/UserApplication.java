package com.userservice.application.adapter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userservice.application.dto.UserContext;
import com.userservice.application.port.in.UserCommandUseCase;
import com.userservice.application.port.out.UserPersistencePort;
import com.userservice.domain.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserApplication implements UserCommandUseCase {

	private final UserPersistencePort userPersistencePort;
	private final EncryptUserFactory encryptUserFactory;

	@Override
	public User create(UserContext userContext) {

		//암호화
		User encryptUser = encryptUserFactory.toEncryptUser(userContext);

		User savedUser = userPersistencePort.save(encryptUser);

		//TODO: kafka 모듈을 따로 분리할지 고료

		// deposit

		// cart

		return savedUser;
	}

	@Override
	public void update(UserContext userContext) {
		User encryptUser = encryptUserFactory.toEncryptUser(userContext);
		userPersistencePort.update(encryptUser);
	}

	@Override
	public void delete(String id) {
		userPersistencePort.withdraw(id);
	}

	@Transactional(readOnly = true)
	@Override
	public User getUser(String id) {
		return userPersistencePort.findById(id);
	}
}
