package com.user.infrastructure.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
	String email,
	@NotBlank(message = "연락처를 입력해주세요")
	String phoneNumber,
	String street,
	String zipCode,
	String state,
	String city,
	String details,
	@NotBlank(message = "이름을 입력해주세요.")
	String name,
	@NotBlank(message = "닉네임을 입력해주세요.")
	String nickname,
	@NotBlank(message = "OAuthId는 필수입니다.")
	String oauthId,

	@NotNull(message = "나이를 입력해주세요.")
	@Min(value = 1, message = "나이는 1 이상이어야 합니다.")
	@Max(value = 150, message = "나이는 150 이하여야 합니다.")
	int age,
	@NotBlank(message = "성별을 입력해주세요.")
	String gender
) {
}
