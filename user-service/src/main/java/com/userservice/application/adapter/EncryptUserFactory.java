package com.userservice.application.adapter;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.userservice.application.dto.UserContext;
import com.userservice.application.utils.EncryptEncoder;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.Address;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class EncryptUserFactory {

	private final PasswordEncoder passwordEncoder;

	public User toEncryptUser(UserContext userContext) {
		return User.builder()
			.email(encode(userContext.email()))
			.password(encode(userContext.password()))
			.name(encode(userContext.name()))
			.phoneNumber(encode(userContext.phoneNumber()))
			.nickname(userContext.nickname())
			.address(
				Address.builder()
					.state(encode(userContext.state()))
					.city(encode(userContext.city()))
					.street(encode(userContext.street()))
					.zipCode(encode(userContext.zipCode()))
					.details(encode(userContext.addressDetails()))
					.build()
			)
			.oauthId(userContext.oauthId())
			.profileUrl(userContext.addressDetails())
			.role(userContext.role())
			.build();
	}

	private String encode(String target) {
		return passwordEncoder.encode(target);
	}
}
