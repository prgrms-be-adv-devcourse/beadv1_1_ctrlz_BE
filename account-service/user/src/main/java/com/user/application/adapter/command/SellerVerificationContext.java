package com.user.application.adapter.command;

import com.common.exception.CustomException;
import com.common.exception.vo.UserExceptionCode;
import com.user.domain.model.User;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class SellerVerificationContext {

	private final String phoneNumber;
	private final String userId;
	private final String verificationCode;

	@Builder
	private SellerVerificationContext(
		String phoneNumber,
		String id,
		String verificationCode
	) {
		this.userId = id;
		this.phoneNumber = phoneNumber;
		this.verificationCode = verificationCode;
	}

	public static SellerVerificationContext forSending(
		String phoneNumber,
		String id,
		User user
	) {

		if (user.getRoles().contains(com.user.domain.vo.UserRole.SELLER)) {
			throw new CustomException(UserExceptionCode.ALREADY_SELLER.getMessage());
		}

		if (!user.getPhoneNumber().equals(phoneNumber)) {
			throw new CustomException(UserExceptionCode.NOT_YOUR_PHONE.getMessage());
		}
		if (phoneNumber.isEmpty() || id.isEmpty()) {
			throw new CustomException("연락처 또는 id가 필요합니다.");
		}

		return SellerVerificationContext.builder()
			.id(id)
			.phoneNumber(phoneNumber)
			.build();
	}

	public static SellerVerificationContext toVerify(
		String id,
		String verificationCode
	) {

		if (verificationCode.isEmpty() || id.isEmpty()) {
			throw new CustomException("연락처 또는 id가 필요합니다.");
		}

		return SellerVerificationContext.builder()
			.id(id)
			.verificationCode(verificationCode)
			.build();
	}

}
