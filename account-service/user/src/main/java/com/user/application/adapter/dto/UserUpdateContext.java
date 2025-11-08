package com.user.application.adapter.dto;

import lombok.Builder;

@Builder
public record UserUpdateContext(
    String nickname,
    String phoneNumber,
    String street,
	String zipCode,
	String state,
	String city,
	String details
) {
}
