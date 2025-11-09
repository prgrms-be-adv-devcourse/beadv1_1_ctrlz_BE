package com.userservice.application.adapter.command;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.common.exception.CustomException;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.UserRole;

class SellerVerificationContextTest {

	@DisplayName("이미 판매자인 경우 예외를 던진다.")
	@Test
	void test1() {
		//given
		User user = User.builder()
			.roles(List.of(UserRole.SELLER))
			.build();

		//when then
		assertThatThrownBy(() -> SellerVerificationContext.forSending("010-1111-1111", "userId", user))
			.isInstanceOf(CustomException.class);
	}

	@DisplayName("연락처가 본인이 아닌 경우 예외를 던진다.")
	@Test
	void test2() {
		//given
		User user = User.builder()
			.roles(List.of(UserRole.USER))
			.phoneNumber("010-1111-1111")
			.build();

		//when then
		assertThatThrownBy(() -> SellerVerificationContext.forSending("010-1311-1111", "userId", user))
			.isInstanceOf(CustomException.class);
	}

	@DisplayName("sms 통신을 위한 객체를 생성할 수 있다")
	@Test
	void test3() {
		//given
		String phoneNumber = "010-1111-1111";
		User user = User.builder()
			.roles(List.of(UserRole.USER))
			.phoneNumber(phoneNumber)
			.build();

		//when
		String userId = "userId";
		SellerVerificationContext result = SellerVerificationContext.forSending(
			phoneNumber,
			userId,
			user
		);

		// then
		assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(result.getUserId()).isEqualTo(userId);
		assertThat(result.getVerificationCode()).isNull();
	}

	@DisplayName("인증을 위한 객체를 생성할 수 있다")
	@Test
	void test4() {
		//given
		String userId = "userId";
		String code = "123456";

		//when
		SellerVerificationContext result = SellerVerificationContext.toVerify(
			userId, code
		);

		// then
		assertThat(result.getUserId()).isEqualTo(userId);
		assertThat(result.getVerificationCode()).isEqualTo(code);
		assertThat(result.getPhoneNumber()).isNull();
	}

}