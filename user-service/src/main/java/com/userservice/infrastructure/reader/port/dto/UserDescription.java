package com.userservice.infrastructure.reader.port.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record UserDescription(
	String name,
	String nickname,
	String phoneNumber,
	String zipCode,
	String state,
	String street,
	String city,
	String details,
	String email,
	List<String> roles
) {
}
