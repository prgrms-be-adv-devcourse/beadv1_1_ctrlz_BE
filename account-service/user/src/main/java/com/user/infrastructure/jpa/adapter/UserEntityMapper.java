package com.user.infrastructure.jpa.adapter;

import com.user.domain.model.User;
import com.user.domain.vo.Address;
import com.user.infrastructure.jpa.entity.UserEntity;
import com.user.infrastructure.jpa.vo.EmbeddedAddress;

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
			.imageId(user.getImageId())
			.password(user.getPassword())
			.profileImageUrl(user.getProfileImageUrl())
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
			.profileImageUrl(userEntity.getProfileImageUrl())
			.nickname(userEntity.getNickname())
			.address(Address.builder()
				.street(userEntity.getAddress().getStreet())
				.city(userEntity.getAddress().getCity())
				.state(userEntity.getAddress().getState())
				.details(userEntity.getAddress().getDetails())
				.zipCode(userEntity.getAddress().getZipCode())
				.build())
			.imageId(userEntity.getImageId())
			.phoneNumber(userEntity.getPhoneNumber())
			.oauthId(userEntity.getOauthId())
			.createdAt(userEntity.getCreatedAt())
			.updatedAt(userEntity.getUpdatedAt())
			.deleteStatus(userEntity.getDeleteStatus())
			.build();
	}

	public static EmbeddedAddress toEmbeddedAddress(Address address) {
		return EmbeddedAddress.builder()
			.city(address.getCity())
			.street(address.getStreet())
			.zipCode(address.getZipCode())
			.state(address.getState())
			.details(address.getDetails())
			.build();
	}
}
