package com.accountapplication.user.infrastructure.jpa.adapter;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.common.model.persistence.BaseEntity;
import com.user.domain.model.User;
import com.user.domain.vo.Address;
import com.user.domain.vo.UserRole;
import com.user.infrastructure.jpa.adapter.UserPersistenceAdapter;
import com.user.infrastructure.jpa.converter.AESUtils;
import com.user.infrastructure.jpa.repository.UserJpaRepository;

@Import({AESUtils.class})
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
			.roles(List.of(UserRole.USER))
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
		assertThat(result.getRoles()).contains(UserRole.USER);
		assertThat(result.getPhoneNumber()).isEqualTo("010-1234-5678");
	}

	@DisplayName("연락처를 통해 객체 유무를 판별할 수 있다.")
	@Test
	void test2() throws Exception {
		//given
		User user = userPersistenceAdapter.save(mockUser);

		//when then
		assertThat(userPersistenceAdapter.existsPhoneNumber(user.getPhoneNumber())).isTrue();
	}

	@DisplayName("닉네임을 통해 유무를 판별할 수 있다.")
	@Test
	void test3() throws Exception {
		//given
		User user = userPersistenceAdapter.save(mockUser);

		//when then
		assertThat(userPersistenceAdapter.existsPhoneNumber(user.getPhoneNumber())).isTrue();
	}

	@Nested
	@DisplayName("사용자 정보 수정 테스트")
	class UpdateUser {

		private User savedUser;

		@BeforeEach
		void setUp() {
			savedUser = userPersistenceAdapter.save(mockUser);
		}

		@DisplayName("닉네임을 수정할 수 있다.")
		@Test
		void test1() {
			// given
			String newNickname = "new_nickname";
			User updateUser = User.builder()
				.id(savedUser.getId())
				.nickname(newNickname)
				.phoneNumber(savedUser.getPhoneNumber())
				.address(savedUser.getAddress())
				.build();

			// when
			userPersistenceAdapter.update(updateUser);

			// then
			User foundUser = userPersistenceAdapter.findById(savedUser.getId());
			assertThat(foundUser.getNickname()).isEqualTo(newNickname);
			assertThat(foundUser.getPhoneNumber()).isEqualTo(savedUser.getPhoneNumber());
			assertThat(foundUser.getAddress()).isEqualTo(savedUser.getAddress());
		}

		@DisplayName("연락처를 수정할 수 있다.")
		@Test
		void test2() {
			// given
			String newPhoneNumber = "010-9999-8888";
			User updateUser = User.builder()
				.id(savedUser.getId())
				.nickname(savedUser.getNickname())
				.phoneNumber(newPhoneNumber)
				.address(savedUser.getAddress())
				.build();

			// when
			userPersistenceAdapter.update(updateUser);

			// then
			User foundUser = userPersistenceAdapter.findById(savedUser.getId());
			assertThat(foundUser.getPhoneNumber()).isEqualTo(newPhoneNumber);
			assertThat(foundUser.getNickname()).isEqualTo(savedUser.getNickname());
			assertThat(foundUser.getAddress()).isEqualTo(savedUser.getAddress());
		}

		@DisplayName("도로명을 수정할 수 있다.")
		@Test
		void test3() {
			// given
			String newStreet = "new_street_address";
			Address updatedAddress = Address.builder()
				.street(newStreet)
				.city(savedUser.getAddress().getCity())
				.state(savedUser.getAddress().getState())
				.details(savedUser.getAddress().getDetails())
				.zipCode(savedUser.getAddress().getZipCode())
				.build();
			User updateUser = User.builder()
				.id(savedUser.getId())
				.nickname(savedUser.getNickname())
				.phoneNumber(savedUser.getPhoneNumber())
				.address(updatedAddress)
				.build();

			// when
			userPersistenceAdapter.update(updateUser);

			// then
			User foundUser = userPersistenceAdapter.findById(savedUser.getId());
			assertThat(foundUser.getAddress().getStreet()).isEqualTo(newStreet);
			assertThat(foundUser.getNickname()).isEqualTo(savedUser.getNickname());
			assertThat(foundUser.getPhoneNumber()).isEqualTo(savedUser.getPhoneNumber());
			assertThat(foundUser.getAddress().getCity()).isEqualTo(savedUser.getAddress().getCity());
			assertThat(foundUser.getAddress().getState()).isEqualTo(savedUser.getAddress().getState());
			assertThat(foundUser.getAddress().getDetails()).isEqualTo(savedUser.getAddress().getDetails());
			assertThat(foundUser.getAddress().getZipCode()).isEqualTo(savedUser.getAddress().getZipCode());
		}

		@DisplayName("시를 수정할 수 있다.")
		@Test
		void test4() {
			// given
			String newCity = "new_city_name";
			Address updatedAddress = Address.builder()
				.street(savedUser.getAddress().getStreet())
				.city(newCity)
				.state(savedUser.getAddress().getState())
				.details(savedUser.getAddress().getDetails())
				.zipCode(savedUser.getAddress().getZipCode())
				.build();
			User updateUser = User.builder()
				.id(savedUser.getId())
				.nickname(savedUser.getNickname())
				.phoneNumber(savedUser.getPhoneNumber())
				.address(updatedAddress)
				.build();

			// when
			userPersistenceAdapter.update(updateUser);

			// then
			User foundUser = userPersistenceAdapter.findById(savedUser.getId());
			assertThat(foundUser.getAddress().getCity()).isEqualTo(newCity);
			assertThat(foundUser.getNickname()).isEqualTo(savedUser.getNickname());
			assertThat(foundUser.getPhoneNumber()).isEqualTo(savedUser.getPhoneNumber());
			assertThat(foundUser.getAddress().getStreet()).isEqualTo(savedUser.getAddress().getStreet());
			assertThat(foundUser.getAddress().getState()).isEqualTo(savedUser.getAddress().getState());
			assertThat(foundUser.getAddress().getDetails()).isEqualTo(savedUser.getAddress().getDetails());
			assertThat(foundUser.getAddress().getZipCode()).isEqualTo(savedUser.getAddress().getZipCode());
		}

		@DisplayName("구/면 등을 수정할 수 있다.")
		@Test
		void test5() {
			// given
			String newState = "new_state_name";
			Address updatedAddress = Address.builder()
				.street(savedUser.getAddress().getStreet())
				.city(savedUser.getAddress().getCity())
				.state(newState)
				.details(savedUser.getAddress().getDetails())
				.zipCode(savedUser.getAddress().getZipCode())
				.build();
			User updateUser = User.builder()
				.id(savedUser.getId())
				.nickname(savedUser.getNickname())
				.phoneNumber(savedUser.getPhoneNumber())
				.address(updatedAddress)
				.build();

			// when
			userPersistenceAdapter.update(updateUser);

			// then
			User foundUser = userPersistenceAdapter.findById(savedUser.getId());
			assertThat(foundUser.getAddress().getState()).isEqualTo(newState);
			assertThat(foundUser.getNickname()).isEqualTo(savedUser.getNickname());
			assertThat(foundUser.getPhoneNumber()).isEqualTo(savedUser.getPhoneNumber());
			assertThat(foundUser.getAddress().getCity()).isEqualTo(savedUser.getAddress().getCity());
			assertThat(foundUser.getAddress().getStreet()).isEqualTo(savedUser.getAddress().getStreet());
			assertThat(foundUser.getAddress().getDetails()).isEqualTo(savedUser.getAddress().getDetails());
			assertThat(foundUser.getAddress().getZipCode()).isEqualTo(savedUser.getAddress().getZipCode());
		}

		@DisplayName("상세 주소를 수정할 수 있다.")
		@Test
		void test6() {
			// given
			String newDetails = "new_details_address";
			Address updatedAddress = Address.builder()
				.street(savedUser.getAddress().getStreet())
				.city(savedUser.getAddress().getCity())
				.state(savedUser.getAddress().getState())
				.details(newDetails)
				.zipCode(savedUser.getAddress().getZipCode())
				.build();
			User updateUser = User.builder()
				.id(savedUser.getId())
				.nickname(savedUser.getNickname())
				.phoneNumber(savedUser.getPhoneNumber())
				.address(updatedAddress)
				.build();

			// when
			userPersistenceAdapter.update(updateUser);

			// then
			User foundUser = userPersistenceAdapter.findById(savedUser.getId());
			assertThat(foundUser.getAddress().getDetails()).isEqualTo(newDetails);
			assertThat(foundUser.getNickname()).isEqualTo(savedUser.getNickname());
			assertThat(foundUser.getPhoneNumber()).isEqualTo(savedUser.getPhoneNumber());
			assertThat(foundUser.getAddress().getCity()).isEqualTo(savedUser.getAddress().getCity());
			assertThat(foundUser.getAddress().getStreet()).isEqualTo(savedUser.getAddress().getStreet());
			assertThat(foundUser.getAddress().getState()).isEqualTo(savedUser.getAddress().getState());
			assertThat(foundUser.getAddress().getZipCode()).isEqualTo(savedUser.getAddress().getZipCode());
		}

		@DisplayName("우편번호를 수정할 수 있다.")
		@Test
		void test7() {
			// given
			String newZipCode = "new_zip_code";
			Address updatedAddress = Address.builder()
				.street(savedUser.getAddress().getStreet())
				.city(savedUser.getAddress().getCity())
				.state(savedUser.getAddress().getState())
				.details(savedUser.getAddress().getDetails())
				.zipCode(newZipCode)
				.build();
			User updateUser = User.builder()
				.id(savedUser.getId())
				.nickname(savedUser.getNickname())
				.phoneNumber(savedUser.getPhoneNumber())
				.address(updatedAddress)
				.build();

			// when
			userPersistenceAdapter.update(updateUser);

			// then
			User foundUser = userPersistenceAdapter.findById(savedUser.getId());
			assertThat(foundUser.getAddress().getZipCode()).isEqualTo(newZipCode);
			assertThat(foundUser.getNickname()).isEqualTo(savedUser.getNickname());
			assertThat(foundUser.getPhoneNumber()).isEqualTo(savedUser.getPhoneNumber());
			assertThat(foundUser.getAddress().getCity()).isEqualTo(savedUser.getAddress().getCity());
			assertThat(foundUser.getAddress().getStreet()).isEqualTo(savedUser.getAddress().getStreet());
			assertThat(foundUser.getAddress().getState()).isEqualTo(savedUser.getAddress().getState());
			assertThat(foundUser.getAddress().getDetails()).isEqualTo(savedUser.getAddress().getDetails());
		}
	}
}