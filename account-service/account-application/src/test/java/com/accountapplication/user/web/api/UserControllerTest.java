package com.accountapplication.user.web.api;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.application.adapter.command.SellerVerificationContext;
import com.user.application.port.in.SellerVerificationUseCase;
import com.user.domain.vo.UserRole;
import com.user.infrastructure.api.dto.UpdateSellerRequest;
import com.user.infrastructure.api.dto.UserCreateRequest;
import com.user.infrastructure.api.dto.UserUpdateRequest;
import com.user.infrastructure.api.dto.VerificationReqeust;
import com.user.infrastructure.feign.CartClient;
import com.user.infrastructure.feign.ProfileImageClient;
import com.user.infrastructure.feign.dto.CartCreateRequest;
import com.user.infrastructure.jpa.entity.UserEntity;
import com.user.infrastructure.jpa.repository.UserJpaRepository;
import com.user.infrastructure.jpa.vo.EmbeddedAddress;

import software.amazon.awssdk.services.s3.S3Client;

@ActiveProfiles("test")
@Transactional
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@MockitoBean
	private S3Client s3Client;

	@MockitoBean
	private ProfileImageClient profileImageClient;

	@MockitoBean
	private CartClient cartClient;

	@MockitoBean
	private SellerVerificationUseCase sellerVerificationUseCase;

	@BeforeEach
	void setUp() {
		userJpaRepository.deleteAllInBatch();
	}

	@DisplayName("신규 유저를 생성한다.")
	@Test
	void test1() throws Exception {
		// given
		UserCreateRequest request = new UserCreateRequest("test@test.com", "010-1111-0111", "street",
			"123423", "state", "city", "details", "name", "nickname", "profileImageUrl");

		when(cartClient.createCart(any(CartCreateRequest.class))).thenReturn(ResponseEntity.status(200).body(any()));

		// when then
		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 완료"))
			.andExpect(jsonPath("$.data.nickname").value("nickname"))
			.andExpect(jsonPath("$.data.profileUrl").value("default_image"))
			.andExpect(jsonPath("$.data.userId").value(Matchers.notNullValue()));
	}

	@DisplayName("카트가 생성되지 않으면 유저가 생성되지 않고 예외를 던진다.")
	@Test
	void test3() throws Exception {
		// given
		UserCreateRequest request = new UserCreateRequest("test@test.com", "010-1111-0111", "street",
			"123423", "state", "city", "details", "name", "nickname", "profileImageUrl");


		when(cartClient.createCart(any(CartCreateRequest.class))).thenReturn(ResponseEntity.status(400).body(any()));

		// when then
		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))
			)
			.andDo(print())
			.andExpect(status().isInternalServerError());
	}

	@DisplayName("not blank 예외 처리")
	@Test
	void test2() throws Exception {
		// given
		UserCreateRequest request = new UserCreateRequest("test@test.com", "", "street",
			"123423", "state", "city", "details", "name", "nickname", "profileImageUrl");

		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.customFieldErrors[0].field").value("phoneNumber"))
			.andExpect(jsonPath("$.customFieldErrors[0].rejectedValue").value(""))
			.andExpect(jsonPath("$.customFieldErrors[0].reason").value("연락처를 입력해주세요"));
	}

	@DisplayName("sms 문자 전송 api")
	@Test
	void test4() throws Exception {
		// given
		UserEntity userEntity = UserEntity.builder()
			.address(EmbeddedAddress.builder()
				.city("서울특별시")
				.street("테헤란로 123")
				.zipCode("06234")
				.state("서울특별시")
				.details("101동 1001호")
				.build())
			.nickname("pillivery_dev")
			.oauthId("GOOGLE")
			.email("minseok@example.com")
			.name("최민석")
			.password("default")
			.profileImageUrl("https://example-bucket.s3.amazonaws.com/profile/default.png")
			.phoneNumber("010-1234-5678")
			.build();

		UserEntity user = userJpaRepository.save(userEntity);

		// when then
		mockMvc.perform(post("/api/users/sellers/verification/{id}", user.getId())
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(new VerificationReqeust("010-1234-5678"))))
			.andDo(print())
			.andExpect(status().isOk());

		verify(sellerVerificationUseCase, times(1)).requestVerificationCode(any(SellerVerificationContext.class));
	}

	@DisplayName("인증 확인 및 seller 추가 api")
	@Test
	void test5() throws Exception {
		// given
		UserEntity userEntity = UserEntity.builder()
			.address(EmbeddedAddress.builder()
				.city("서울특별시")
				.street("테헤란로 123")
				.zipCode("06234")
				.state("서울특별시")
				.details("101동 1001호")
				.build())
			.nickname("pillivery_dev")
			.oauthId("GOOGLE")
			.email("minseok@example.com")
			.name("최민석")
			.password("default")
			.profileImageUrl("https://example-bucket.s3.amazonaws.com/profile/default.png")
			.phoneNumber("010-1234-5678")
			.build();

		UserEntity user = userJpaRepository.save(userEntity);

		doNothing().when(sellerVerificationUseCase).checkVerificationCode(any(SellerVerificationContext.class));

		mockMvc.perform(post("/api/users/sellers/{id}", user.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new UpdateSellerRequest("010-1234-5678"))))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("판매자 등록이 완료됐습니다."));

		assertThat(user.getRoles()).contains(UserRole.SELLER);

	}

	@DisplayName("유저 정보 수정 api")
	@Test
	void test6() throws Exception {
		// given
		UserEntity userEntity = UserEntity.builder()
			.address(EmbeddedAddress.builder()
				.city("old_city")
				.street("old_street")
				.zipCode("old_zipCode")
				.state("old_state")
				.details("old_details")
				.build())
			.nickname("old_nickname")
			.oauthId("GOOGLE")
			.email("test@example.com")
			.name("test_name")
			.password("default")
			.profileImageUrl("https://example-bucket.s3.amazonaws.com/profile/default.png")
			.phoneNumber("010-1234-5678")
			.build();

		UserEntity savedUser = userJpaRepository.save(userEntity);

		UserUpdateRequest updateRequest = new UserUpdateRequest(
			"new_nickname",
			"010-1234-5678",
			"new_street",
			"new_zipCode",
			"new_state",
			"new_city",
			"new_details"
		);

		// when
		mockMvc.perform(patch("/api/users/{id}", savedUser.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andDo(print())
			.andExpect(status().isOk());

		// then
		UserEntity updatedUser = userJpaRepository.findById(savedUser.getId()).orElseThrow();
		assertThat(updatedUser.getNickname()).isEqualTo("new_nickname");
		assertThat(updatedUser.getAddress().getCity()).isEqualTo("new_city");
		assertThat(updatedUser.getAddress().getStreet()).isEqualTo("new_street");
		assertThat(updatedUser.getAddress().getZipCode()).isEqualTo("new_zipCode");
		assertThat(updatedUser.getAddress().getState()).isEqualTo("new_state");
		assertThat(updatedUser.getAddress().getDetails()).isEqualTo("new_details");
	}
}
