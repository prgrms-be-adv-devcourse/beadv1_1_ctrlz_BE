package com.user.infrastructure.api.mapper;

import com.user.application.adapter.dto.UserContext;
import com.user.application.adapter.dto.UserUpdateContext;
import com.user.infrastructure.api.dto.UserCreateRequest;
import com.user.infrastructure.api.dto.UserUpdateRequest;

public class UserContextMapper {
	public static UserContext toContext(UserCreateRequest request, String imageUrl) {
		return UserContext.builder()
			.phoneNumber(request.phoneNumber())
			.addressDetails(request.details())
			.email(request.email())
			.name(request.name())
			.password("default")
			.state(request.state())
			.street(request.street())
			.city(request.city())
			.zipCode(request.zipCode())
			.nickname(request.nickname())
			.oauthId(request.oauthId())
			.profileImageUrl(imageUrl)
			.imageId("default_Image")
			.build();
	}

	public static UserUpdateContext toContext(UserUpdateRequest request) {
		return UserUpdateContext.builder()
			.nickname(request.nickname())
			.phoneNumber(request.phoneNumber())
			.street(request.street())
			.zipCode(request.zipCode())
			.state(request.state())
			.city(request.city())
			.details(request.details())
			.build();
	}
}
