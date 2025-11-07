package com.userservice.application.adapter.dto;

import lombok.Builder;

@Builder
public record UserContext(
	String email,
	String nickname,
	String name,
	String password,
	String state,
	String zipCode,
	String street,
	String city,
	String addressDetails,
	String phoneNumber,
	String oauthId,
	String profileImageUrl
) {

}
