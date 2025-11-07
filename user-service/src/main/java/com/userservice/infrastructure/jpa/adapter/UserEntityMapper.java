package com.userservice.infrastructure.jpa.adapter;

import com.userservice.domain.model.User;
import com.userservice.domain.vo.Address;
import com.userservice.infrastructure.jpa.entity.UserEntity;
import com.userservice.infrastructure.jpa.vo.EmbeddedAddress;

public class UserEntityMapper {

	public static UserEntity toEntity(User user) {

		return UserEntity.builder()
			.address(EmbeddedAddress.builder()
				.city(user.getAddress().getCity())
				.street(user.getAddress().getStreet())
				.zipCode(user.getAddress().getZipCode())
				.state(user.getAddress().getState())
				.details(user.getAddress().getDetails())
				.build())
			.nickname(user.getNickname())
			.oauthId(user.getOauthId())
			.email(user.getEmail())
			.name(user.getName())
			.password(user.getPassword())
			.profileUrl(user.getProfileUrl())
			.phoneNumber(user.getPhoneNumber())
			.build();
	}

	public static User toDomain(UserEntity userEntity) {

		return User.builder()
			.id(userEntity.getId())
			.name(userEntity.getName())
			.password(userEntity.getPassword())
			.email(userEntity.getEmail())
			.roles(userEntity.getRoles())
			.profileUrl(userEntity.getProfileUrl())
			.nickname(userEntity.getNickname())
			.address(Address.builder()
				.street(userEntity.getAddress().getStreet())
				.city(userEntity.getAddress().getCity())
				.state(userEntity.getAddress().getState())
				.details(userEntity.getAddress().getDetails())
				.zipCode(userEntity.getAddress().getZipCode())
				.build())
			.phoneNumber(userEntity.getPhoneNumber())
			.oauthId(userEntity.getOauthId())
			.createdAt(userEntity.getCreatedAt())
			.updatedAt(userEntity.getUpdatedAt())
			.deleteStatus(userEntity.getDeleteStatus())
			.build();
	}
}
