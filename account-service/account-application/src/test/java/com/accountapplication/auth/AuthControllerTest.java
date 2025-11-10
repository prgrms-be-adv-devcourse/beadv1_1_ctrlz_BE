package com.accountapplication.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.auth.dto.TokenRefreshRequest;
import com.auth.jwt.JwtTokenProvider;
import com.auth.service.JwtAuthService;
import com.user.infrastructure.redis.configuration.EmbeddedRedisConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@Import(EmbeddedRedisConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthService jwtAuthService;

    @DisplayName("토큰 재발급 테스트")
    @Test
    void refreshToken() throws Exception {
        // given
        TokenRefreshRequest request = new TokenRefreshRequest("testUser", "valid-refresh-token");
        String newAccessToken = "new-access-token";
        long expiration = Instant.now().getEpochSecond() + 3600;

        given(jwtAuthService.reissueAccessToken(anyString(), anyString())).willReturn(newAccessToken);
        given(jwtTokenProvider.getExpirationFromToken(newAccessToken)).willReturn(Instant.ofEpochSecond(expiration));

        // when then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(cookie().exists("accessToken"))
            .andExpect(cookie().value("accessToken", newAccessToken))
            .andExpect(cookie().httpOnly("accessToken", true))
            .andExpect(cookie().secure("accessToken", false))
            .andExpect(cookie().path("accessToken", "/"))
            .andExpect(cookie().sameSite("accessToken", "Lax"));
    }

    @DisplayName("로그아웃 테스트")
    @Test
    void logout() throws Exception {
        // given
        String token = "valid-access-token";
        String userId = "testUser";

        given(jwtTokenProvider.getUserIdFromToken(token)).willReturn(userId);

        // when then
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andDo(print())
            .andExpect(status().isOk());

        verify(jwtAuthService).logout(userId, token);
    }
}
