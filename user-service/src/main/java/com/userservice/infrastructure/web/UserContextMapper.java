package com.userservice.infrastructure.web;

import com.userservice.application.adapter.dto.UserContext;
import com.userservice.domain.vo.OAuthId;
import com.userservice.infrastructure.web.dto.UserCreateRequest;

public class UserContextMapper {
	public static UserContext toContext(UserCreateRequest request) {
		return UserContext.builder()
			.phoneNumber(request.phoneNumber())
			.addressDetails(request.details())
			.email(request.email())
			.name(request.name())
			.password(request.password().isBlank() ? "default" : request.password())
			.state(request.state())
			.street(request.street())
			.city(request.city())
			.zipCode(request.zipCode())
			.nickname(request.nickname())
			.oauthId(OAuthId.GOOGLE.name())
			.build();
	}
}
