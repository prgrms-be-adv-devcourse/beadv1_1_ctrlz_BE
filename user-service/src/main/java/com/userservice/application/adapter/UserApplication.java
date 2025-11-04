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

		verifyPhoneNumber(userContext.phoneNumber());
		verifyNickname(userContext.nickname());

		//암호화
		User encryptUser = encryptUserFactory.toEncryptUser(userContext);

		User savedUser = userPersistencePort.save(encryptUser);

		//TODO: kafka 모듈을 따로 분리할지 고료

		// deposit

		// cart

		return savedUser;
	}

	private void verifyNickname(String nickname) {
		if (userPersistencePort.existsNickname(nickname)) {
			throw new IllegalStateException("이미 존재하는 닉네임 " + nickname);
		}
	}

	private void verifyPhoneNumber(String phoneNumber) {
		if (userPersistencePort.existsPhoneNumber(phoneNumber)) {
			throw new IllegalStateException("이미 존재하는 연락처");
		}
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
