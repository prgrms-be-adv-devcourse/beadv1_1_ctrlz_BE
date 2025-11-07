package com.userservice.infrastructure.reader.adapter;

import org.springframework.stereotype.Component;

import com.userservice.application.port.out.UserPersistencePort;
import com.userservice.domain.model.User;
import com.userservice.infrastructure.reader.port.UserReaderPort;
import com.userservice.infrastructure.reader.port.dto.UserDescription;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserReaderAdapter implements UserReaderPort {

	private final UserPersistencePort userPersistencePort;
	@Override
	public UserDescription getUserDescription(String id) {

		User user = userPersistencePort.findById(id);

		return UserDescription.builder()
			.city(user.getAddress().getCity())
			.roles(user.getRolesToString())
			.email(user.getEmail())
			.state(user.getAddress().getState())
			.city(user.getAddress().getCity())
			.street(user.getAddress().getStreet())
			.zipCode(user.getAddress().getZipCode())
			.phoneNumber(user.getPhoneNumber())
			.details(user.getAddress().getDetails())
			.name(user.getName())
			.nickname(user.getNickname())
			.build();
	}
}
