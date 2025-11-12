package com.user.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
	@NotBlank(message = "닉네임을 입력해주세요.")
	String nickname,
	@NotBlank(message = "연락처를 입력해주세요.")
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
	String details
) {
}
