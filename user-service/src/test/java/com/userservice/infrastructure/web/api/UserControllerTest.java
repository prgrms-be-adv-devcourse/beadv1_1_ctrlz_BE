package com.userservice.infrastructure.web.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.infrastructure.web.dto.UserCreateRequest;

@ActiveProfiles("test")
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@DisplayName("신규 유저를 생성한다.")
	@Test
	void test1() throws Exception {
		// given
		UserCreateRequest request = new UserCreateRequest("test@test.com", "password", "010-1111-0111", "street",
			"123423", "state", "city", "details", "name", "nickname", "profileImageUrl");

		// when & then
		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 완료"))
			.andExpect(jsonPath("$.data.nickname").value("nickname"))
			.andExpect(jsonPath("$.data.profileUrl").value("profileImageUrl"))
			.andExpect(jsonPath("$.data.userId").value(Matchers.notNullValue()));
	}

	@DisplayName("not blank 예외 처리")
	@Test
	void test2() throws Exception {
		// given
		UserCreateRequest request = new UserCreateRequest("test@test.com", "password", "", "street",
			"123423", "state", "city", "details", "name", "nickname", "profileImageUrl");

		// when & then
		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.customFieldErrors[0].field").value("phoneNumber"))
			.andExpect(jsonPath("$.customFieldErrors[0].rejectedValue").value(""))
			.andExpect(jsonPath("$.customFieldErrors[0].reason").value("연락처를 입력해주세요"));
	}
}
