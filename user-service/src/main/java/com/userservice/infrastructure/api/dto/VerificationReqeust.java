package com.userservice.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;

public record VerificationReqeust(
	@NotBlank(message = "연락처를 입력해주세요")
	String phoneNumber
) {
}
