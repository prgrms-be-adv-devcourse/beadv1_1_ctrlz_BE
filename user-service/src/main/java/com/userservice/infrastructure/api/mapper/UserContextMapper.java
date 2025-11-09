package com.userservice.infrastructure.api.mapper;

import com.userservice.application.adapter.dto.UserContext;
import com.userservice.application.adapter.dto.UserUpdateContext;
import com.userservice.domain.vo.OAuthId;
import com.userservice.infrastructure.api.dto.UserCreateRequest;
import com.userservice.infrastructure.api.dto.UserUpdateRequest;
import com.userservice.infrastructure.writer.dto.ImageResponse;

public class UserContextMapper {
	public static UserContext toContext(UserCreateRequest request, ImageResponse imageResponse) {
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
			.oauthId(OAuthId.GOOGLE.name())
			.profileImageUrl(imageResponse.imageUrl())
			.imageId(imageResponse.imageId() == null ? "default_Image" : imageResponse.imageId())
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
