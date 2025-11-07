package com.userservice.infrastructure.api.mapper;

import com.userservice.application.adapter.dto.UserContext;
import com.userservice.domain.vo.OAuthId;
import com.userservice.infrastructure.api.dto.UserCreateRequest;

public class UserContextMapper {
	public static UserContext toContext(UserCreateRequest request, String profileUrl) {
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
			.profileImageUrl(profileUrl)
			.build();
	}
}
