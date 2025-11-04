package com.userservice.application.adapter;

import org.springframework.stereotype.Component;

import com.userservice.application.dto.UserContext;
import com.userservice.application.utils.EncryptEncoder;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.Address;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class EncryptUserFactory {

	private final EncryptEncoder encryptEncoder;

	public User toEncryptUser(UserContext userContext) {
		return User.builder()
			.email(encryptEncoder.encode(userContext.email()))
			.password(encryptEncoder.encode(userContext.password()))
			.name(encryptEncoder.encode(userContext.name()))
			.address(
				Address.builder()
					.state(encryptEncoder.encode(userContext.state()))
					.city(encryptEncoder.encode(userContext.city()))
					.street(encryptEncoder.encode(userContext.state()))
					.zipCode(encryptEncoder.encode(userContext.zipCode()))
					.details(encryptEncoder.encode(userContext.addressDetails()))
					.build()
			)
			.profileUrl(userContext.addressDetails())
			.role(userContext.role())
			.build();
	}

}
