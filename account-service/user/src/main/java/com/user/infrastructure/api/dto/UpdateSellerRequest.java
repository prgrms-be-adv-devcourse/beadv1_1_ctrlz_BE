package com.user.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSellerRequest(
	@NotBlank(message = "인증번호가 있어야합니다.")
	String verificationCode
) {
}
