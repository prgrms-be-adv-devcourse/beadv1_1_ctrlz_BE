package com.user.application.adapter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.UserExceptionCode;
import com.user.application.adapter.dto.UserContext;
import com.user.application.adapter.dto.UserUpdateContext;
import com.user.application.port.in.UserCommandUseCase;
import com.user.application.port.out.UserPersistencePort;
import com.user.domain.event.UserSignedUpEvent;
import com.user.domain.model.User;
import com.user.domain.vo.Address;
import com.user.domain.vo.EventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserApplication implements UserCommandUseCase {

	private final UserPersistencePort userPersistencePort;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	public UserContext create(UserContext userContext) {
		verifyNickname(userContext.nickname());
		verifyPhoneNumber(userContext.phoneNumber());

		User user = generateUser(userContext);
		User savedUser = userPersistencePort.save(user);

		applicationEventPublisher.publishEvent(UserSignedUpEvent.from(savedUser.getId(), EventType.CREATED));

		return UserContext.builder()
			.nickname(savedUser.getNickname())
			.profileImageUrl(savedUser.getProfileImageUrl())
			.userId(savedUser.getId())
			.build();
	}

	@Override
	public void updateForSeller(String id) {
		userPersistencePort.updateRolesForSeller(id);
	}

	@Override
	public void updateUser(String userId, UserUpdateContext updateContext) {
		User user = userPersistencePort.findById(userId);

		Address updatedAddress = Address.builder()
			.state(updateContext.state())
			.city(updateContext.city())
			.street(updateContext.street())
			.zipCode(updateContext.zipCode())
			.details(updateContext.details())
			.build();

		updateAddress(user, updatedAddress);
		updatePhoneNumber(updateContext, user);
		updateNickname(updateContext, user);

		userPersistencePort.update(user);
	}

	@Override
	public void updateImage(String userId, String imageId, String profileImageUrl) {
		userPersistencePort.updateImage(userId, imageId, profileImageUrl);
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
			.imageId(userContext.imageId())
			.oauthId(userContext.oauthId())
			.profileImageUrl(userContext.profileImageUrl())
			.build();
	}

	private void verifyNickname(String nickname) {
		if (userPersistencePort.existsNickname(nickname)) {
			throw new CustomException(UserExceptionCode.DUPLICATED_NICKNAME.getMessage());
		}
	}

	private void verifyPhoneNumber(String phoneNumber) {
		if (userPersistencePort.existsPhoneNumber(phoneNumber)) {
			throw new CustomException(UserExceptionCode.DUPLICATED_PHONE_NUMBER.getMessage());
		}
	}

	private void updateNickname(com.user.application.adapter.dto.UserUpdateContext updateContext, User user) {
		if (!user.getNickname().equals(updateContext.nickname())) {
			user.updateNickname(updateContext.nickname());
		}
	}

	private void updatePhoneNumber(UserUpdateContext updateContext, User user) {
		if (!user.getPhoneNumber().equals(updateContext.phoneNumber())) {
			user.updatePhoneNumber(updateContext.phoneNumber());
		}
	}

	private void updateAddress(User user, Address updatedAddress) {
		if (!user.getAddress().equals(updatedAddress)) {
			user.updateAddress(updatedAddress);
		}
	}
}
