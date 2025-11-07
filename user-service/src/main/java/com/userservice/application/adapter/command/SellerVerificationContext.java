package com.userservice.application.adapter.command;

import com.common.exception.CustomException;
import com.common.exception.vo.UserExceptionCode;
import com.userservice.application.port.in.UserCommandUseCase;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.UserRole;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SellerVerificationContext {

	private final String phoneNumber;

	@Builder
	private SellerVerificationContext(String phoneNumber, String id, UserCommandUseCase userCommandUseCase) {
		User user = userCommandUseCase.getUser(id);

		if (user.getRoles().contains(UserRole.SELLER)) {
			throw new CustomException(UserExceptionCode.ALREADY_SELLER.getMessage());
		}

		if (!user.getPhoneNumber().equals(phoneNumber)) {
			throw new CustomException(UserExceptionCode.NOT_YOUR_PHONE.getMessage());
		}

		this.phoneNumber = phoneNumber;
	}

	public static SellerVerificationContext of(String phoneNumber, String id, UserCommandUseCase userCommandUseCase) {
		return SellerVerificationContext.builder()
			.userCommandUseCase(userCommandUseCase)
			.id(id)
			.phoneNumber(phoneNumber)
			.build();
	}
}
