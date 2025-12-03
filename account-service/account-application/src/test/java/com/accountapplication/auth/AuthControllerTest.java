package com.accountapplication.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.auth.jwt.JwtTokenProvider;
import com.auth.service.JwtAuthService;
import com.user.infrastructure.redis.configuration.EmbeddedRedisConfiguration;

import jakarta.servlet.http.Cookie;

@ActiveProfiles("test")
@Import({EmbeddedRedisConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private JwtAuthService jwtAuthService;

	@DisplayName("토큰 재발급 테스트")
	@Test
	void test1() throws Exception {
		// given
		String userId = "testUser";
		String refreshToken = "valid-refresh-token";
		String newAccessToken = "new-access-token";

		given(jwtAuthService.reissueAccessToken(eq(userId), eq(refreshToken))).willReturn(newAccessToken);

		// when then
		mockMvc.perform(post("/api/auth/reissue")
				.header("X-REQUEST-ID", userId)
				.cookie(new Cookie("REFRESH_TOKEN", refreshToken)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().exists("Set-Cookie"));

		verify(jwtAuthService).reissueAccessToken(userId, refreshToken);
	}

	@DisplayName("로그아웃 테스트")
	@Test
	void test2() throws Exception {
		// given
		String token = "valid-access-token";
		String userId = "testUser";

		given(jwtTokenProvider.getUserIdFromToken(token)).willReturn(userId);

		// when then
		mockMvc.perform(get("/api/auth/logout")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().exists("Set-Cookie"));

		verify(jwtAuthService).logout(userId);
		verify(jwtTokenProvider).getUserIdFromToken(token);
	}
}
