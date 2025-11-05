package com.userservice.infrastructure.jpa.adapter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.common.model.persistence.BaseEntity;
import com.userservice.domain.model.User;
import com.userservice.domain.vo.Address;
import com.userservice.domain.vo.UserRole;
import com.userservice.infrastructure.jpa.infrastructure.UserJpaRepository;

@DataJpaTest
class UserPersistenceAdapterTest {

	@Autowired
	private UserJpaRepository userJpaRepository;
	private User mockUser;
	private UserPersistenceAdapter userPersistenceAdapter;

	@BeforeEach
	void setUp() {
		userJpaRepository.deleteAllInBatch();

		mockUser = User.builder()
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

		userPersistenceAdapter = new UserPersistenceAdapter(userJpaRepository);

	}

	@DisplayName("UserEntity를 저장할 수 있다.")
	@Test
	void test1() throws Exception {
		//given
		//when
		User result = userPersistenceAdapter.save(mockUser);

		//then
		assertThat(result.getCreatedAt()).isNotNull();
		assertThat(result.getUpdatedAt()).isNotNull();
		assertThat(result.getId()).isInstanceOf(String.class);
		assertThat(result.getDeleteStatus()).isEqualByComparingTo(BaseEntity.DeleteStatus.N);
		assertThat(result.getRole()).isEqualByComparingTo(UserRole.USER);
		assertThat(result.getPhoneNumber()).isEqualTo("010-1234-5678");
	}

	@DisplayName("연락처를 통해 객체 유무를 판별할 수 있다.")
	@Test
	void test2() throws Exception {
		//given
		User save = userPersistenceAdapter.save(mockUser);
		System.out.println(save.getPhoneNumber());

		//when then
		assertThat(userPersistenceAdapter.existsPhoneNumber(mockUser.getPhoneNumber())).isTrue();
	}

	@DisplayName("닉네임을 통해 유무를 판별할 수 있다.")
	@Test
	void test3() throws Exception {
		//given
		User save = userPersistenceAdapter.save(mockUser);
		System.out.println(save.getPhoneNumber());

		//when then
		assertThat(userPersistenceAdapter.existsPhoneNumber(mockUser.getPhoneNumber())).isTrue();
	}
}