package com.userservice.infrastructure.adapter.out;

import java.util.UUID;

import com.userservice.domain.model.User;
import com.userservice.domain.vo.Address;
import com.userservice.infrastructure.model.vo.EmbeddedAddress;
import com.userservice.infrastructure.model.entity.UserEntity;

public class UserEntityMapper {

	public static UserEntity toEntity(User user) {

		return UserEntity.builder()
			.id(UUID.randomUUID().toString())
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
			.role(user.getRole())
			.build();

	}

	public static User toDomain(UserEntity userEntity) {

		return User.builder()
			.id(userEntity.getId())
			.name(userEntity.getName())
			.password(userEntity.getPassword())
			.email(userEntity.getEmail())
			.role(userEntity.getRole())
			.profileUrl(userEntity.getProfileUrl())
			.nickname(userEntity.getNickname())
			.address(Address.builder()
				.street(userEntity.getAddress().getStreet())
				.city(userEntity.getAddress().getCity())
				.state(userEntity.getAddress().getState())
				.details(userEntity.getAddress().getDetails())
				.build())
			.phoneNumber(userEntity.getPhoneNumber())
			.oauthId(userEntity.getOAuthId())
			.createdAt(userEntity.getCreatedAt())
			.updatedAt(userEntity.getUpdatedAt())
			.build();
	}
}
