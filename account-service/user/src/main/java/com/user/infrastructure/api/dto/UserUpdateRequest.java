package com.user.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
	@NotBlank(message = "닉네임을 입력해주세요.")
	String nickname,
	@NotBlank(message = "연락처를 입력해주세요.")
	String phoneNumber,
	String street,
	String zipCode,
	String state,
	String city,
	String details
) {
}
