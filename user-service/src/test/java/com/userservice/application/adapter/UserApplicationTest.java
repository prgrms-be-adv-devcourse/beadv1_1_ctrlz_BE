package com.userservice.application.adapter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.common.exception.CustomException;
import com.userservice.application.adapter.dto.UserContext;
import com.userservice.infrastructure.jpa.converter.AESUtils;

@Import({AESUtils.class})
@DataJpaTest
class UserApplicationTest {

	@DisplayName("회원 가입 시, 닉네임이 중복되면 예외를 던진다.")
	@Test
	void test1() throws Exception {
		//given
		UserApplication userApplication = new UserApplication(new FakeRepository(), null, null);
		//when//then
		assertThatThrownBy(() -> userApplication.verifyNickname("test_nickname"))
			.isInstanceOf(CustomException.class);
	}

	@DisplayName("회원 가입 시, 연락처가 중복되면 예외를 던진다.")
	@Test
	void test2() throws Exception {
		//given
		UserApplication userApplication = new UserApplication(new FakeRepository(), null, null);
		//when//then
		assertThatThrownBy(() -> userApplication.verifyPhoneNumber("010-1111-1111"))
			.isInstanceOf(CustomException.class);
	}

	@DisplayName("카트 생성에 실패하면 예외를 던진다.")
	@Test
	void test3() throws Exception {
		//given
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		UserApplication userApplication = new UserApplication(new FakeRepository(), passwordEncoder, cartCreateRequest -> ResponseEntity.badRequest().build());
		UserContext mockUser = UserContext.builder()
			.email("mockuser@example.com")
			.nickname("mockNick")
			.name("Mock User")
			.password("password!@#")
			.state("active")
			.zipCode("54321")
			.street("테스트거리 45")
			.city("부산광역시 해운대구")
			.addressDetails("203호")
			.phoneNumber("010-9876-5432")
			.oauthId("oauth-abcdef123456")
			.profileImageUrl("https://cdn.example.com/profile/mockuser.png")
			.build();

		//when then
		assertThatThrownBy(() -> userApplication.create(mockUser))
			.isInstanceOf(CustomException.class);
	}
}