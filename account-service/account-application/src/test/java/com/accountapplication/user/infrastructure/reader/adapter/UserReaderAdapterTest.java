package com.accountapplication.user.infrastructure.reader.adapter;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.user.domain.model.User;
import com.user.domain.vo.Address;
import com.user.domain.vo.UserRole;
import com.user.infrastructure.jpa.adapter.UserPersistenceAdapter;
import com.user.infrastructure.jpa.converter.AESUtils;
import com.user.infrastructure.jpa.repository.UserJpaRepository;
import com.user.infrastructure.reader.port.UserReaderPort;
import com.user.infrastructure.reader.port.dto.UserDescription;

@Import(AESUtils.class)
@DataJpaTest
class UserReaderAdapterTest {

	@Autowired
	private UserJpaRepository userJpaRepository;

	@DisplayName("회원 정보를 조회할 수 있다.")
	@Test
	void test1() throws Exception {
		//given
		User mockUser = User.builder()
			.email("testuser@example.com")
			.nickname("tester")
			.name("테스트 유저")
			.password("encodedPassword123!")
			.address(
				Address.builder()
					.state("경기도")
					.zipCode("12345")
					.street("테스트로 123")
					.city("성남시")
					.details("101동 202호")
					.build()
			)
			.roles(List.of(UserRole.USER))
			.phoneNumber("010-1234-5678")
			.oauthId("kakao_1234567890")
			.build();

		UserPersistenceAdapter userPersistenceAdapter = new UserPersistenceAdapter(userJpaRepository);
		User target = userPersistenceAdapter.save(mockUser);

		UserReaderPort userReaderPort = new com.user.infrastructure.reader.adapter.UserReaderAdapter(new UserPersistenceAdapter(userJpaRepository));

		//when
		UserDescription result = userReaderPort.getUserDescription(target.getId());

		//then
		assertThat(result.city()).isEqualTo(target.getAddress().getCity());
		assertThat(result.details()).isEqualTo(target.getAddress().getDetails());
		assertThat(result.state()).isEqualTo(target.getAddress().getState());
		assertThat(result.zipCode()).isEqualTo(target.getAddress().getZipCode());
		assertThat(result.street()).isEqualTo(target.getAddress().getStreet());
		assertThat(result.phoneNumber()).isEqualTo(target.getPhoneNumber());
		assertThat(result.nickname()).isEqualTo(target.getNickname());
		assertThat(result.name()).isEqualTo(target.getName());
		assertThat(result.email()).isEqualTo(target.getEmail());
		assertThat(result.roles()).contains(UserRole.USER.name());
	}

}