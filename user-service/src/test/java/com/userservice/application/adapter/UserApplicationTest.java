package com.userservice.application.adapter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.common.model.persistence.BaseEntity.DeleteStatus;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.Address;
import com.userservice.domain.vo.UserRole;
import com.userservice.infrastructure.jpa.adapter.UserPersistenceAdapter;
import com.userservice.infrastructure.jpa.infrastructure.UserJpaRepository;

@DataJpaTest
class UserApplicationTest {

	@Autowired
	private UserJpaRepository userJpaRepository;

	@DisplayName("UserEntity를 저장할 수 있다.")
	@Test
	void test1() throws Exception {
		//given
		var mockUser = User.builder()
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
			.role(UserRole.USER)
			.phoneNumber("010-1234-5678")
			.oauthId("kakao_1234567890")
			.build();

		var userPersistenceAdapter = new UserPersistenceAdapter(userJpaRepository);

		//when
		User result = userPersistenceAdapter.save(mockUser);

		//then
		assertThat(result.getCreatedAt()).isNotNull();
		assertThat(result.getUpdatedAt()).isNotNull();
		assertThat(result.getId()).isInstanceOf(String.class);
		assertThat(result.getDeleteStatus()).isEqualByComparingTo(DeleteStatus.N);
		assertThat(result.getRole()).isEqualByComparingTo(UserRole.USER);
	}

}