package com.user.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
	String email,
	@NotBlank(message = "연락처를 입력해주세요")
	String phoneNumber,
	@NotBlank(message = "거리를 입력해주세요.")
	String street,
	@NotBlank(message = "우편번호를 입력해주세요.")
	String zipCode,
	@NotBlank(message = "군/구/면을 입력해주세요.")
	String state,
	@NotBlank(message = "광역시를 입력해 주세요")
	String city,
	@NotBlank(message = "상세주소를 입력해주세요.")
	String details,
	@NotBlank(message = "이름을 입력해주세요.")
	String name,
	@NotBlank(message = "닉네임을 입력해주세요.")
	String nickname,
	@NotBlank(message = "OAuthId는 필수입니다.")
	String oauthId
) {
}
