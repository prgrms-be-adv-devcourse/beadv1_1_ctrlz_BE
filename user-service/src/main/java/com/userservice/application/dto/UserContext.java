package com.userservice.application.dto;

import com.userservice.domain.vo.UserRole;

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
	UserRole role,
	String phoneNumber,
	String oauthId
) {

}
