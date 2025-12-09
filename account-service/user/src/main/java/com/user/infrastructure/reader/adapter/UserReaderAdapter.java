package com.user.infrastructure.reader.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.user.application.port.out.UserPersistencePort;
import com.user.domain.model.User;
import com.user.infrastructure.reader.port.UserReaderPort;
import com.user.infrastructure.reader.port.dto.UserDemographicDescription;
import com.user.infrastructure.reader.port.dto.UserDescription;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Component
public class UserReaderAdapter implements UserReaderPort {

	private final UserPersistencePort userPersistencePort;

	@Override
	public UserDescription getUserDescription(String id) {

		User user = userPersistencePort.findById(id);

		return UserDescription.builder()
			.userId(user.getId())
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
			.imageId(user.getImageId())
			.profileImageUrl(user.getProfileImageUrl())
			.build();
	}

	@Override
	public UserDemographicDescription getUserDemographicDescription(String id) {
		User user = userPersistencePort.findById(id);

		return UserDemographicDescription.builder()
			.age(user.getAge())
			.gender(user.getGender())
			.build();
	}
}
