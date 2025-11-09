package com.userservice.application.adapter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.UserExceptionCode;
import com.userservice.application.adapter.dto.UserContext;
import com.userservice.application.port.in.UserCommandUseCase;
import com.userservice.application.port.out.UserPersistencePort;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.Address;
import com.userservice.domain.vo.UserRole;
import com.userservice.infrastructure.writer.CartClient;
import com.userservice.infrastructure.writer.dto.CartCreateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserApplication implements UserCommandUseCase {

	private final UserPersistencePort userPersistencePort;
	private final PasswordEncoder passwordEncoder;
	private final CartClient cartClient;

	@Override
	public User create(UserContext userContext) {
		verifyNickname(userContext.nickname());
		verifyPhoneNumber(userContext.phoneNumber());

		User user = generateUser(userContext);
		User savedUser = userPersistencePort.save(user);

		ResponseEntity<?> response = cartClient.createCart(new CartCreateRequest(savedUser.getId()));

		if(!response.getStatusCode().is2xxSuccessful()) {
			userPersistencePort.delete(savedUser.getId());

			throw new RuntimeException("카트 생성 실패");
		}

		return savedUser;
	}

	@Override
	public void updateForSeller(String id) {
		userPersistencePort.updateRole(id, UserRole.SELLER);
	}

	@Override
	public void update(UserContext userContext) {
		User user = generateUser(userContext);

		userPersistencePort.update(user);
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

	private User generateUser(UserContext userContext) {
		return User.builder()
			.email(userContext.email())
			.password(passwordEncoder.encode(userContext.password()))
			.name(userContext.name())
			.phoneNumber(userContext.phoneNumber())
			.nickname(userContext.nickname())
			.address(
				Address.builder()
					.state(userContext.state())
					.city(userContext.city())
					.street(userContext.street())
					.zipCode(userContext.zipCode())
					.details(userContext.addressDetails())
					.build()
			)
			.oauthId(userContext.oauthId())
			.profileUrl(userContext.profileImageUrl())
			.build();
	}

	void verifyNickname(String nickname) {
		if (userPersistencePort.existsNickname(nickname)) {
			throw new CustomException(UserExceptionCode.DUPLICATED_NICKNAME.getMessage());
		}
	}

	void verifyPhoneNumber(String phoneNumber) {
		if (userPersistencePort.existsPhoneNumber(phoneNumber)) {
			throw new CustomException(UserExceptionCode.DUPLICATED_PHONE_NUMBER.getMessage());
		}
	}
}
