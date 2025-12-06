package com.aiservice.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aiservice.application.SessionService;
import com.aiservice.domain.model.RecommendationResult;
import com.aiservice.domain.vo.RecommendationStatus;

@WebMvcTest(RecommendationPollingController.class)
class RecommendationPollingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @DisplayName("추천 데이터 폴링 조회")
    @Test
    void test1() throws Exception {
        // given
        String userId = "user1";
        RecommendationResult mockResult = RecommendationResult.builder()
                .status(RecommendationStatus.OK)
                .build();

        given(sessionService.getRecommendations(userId)).willReturn(mockResult);

        // when then
        mockMvc.perform(get("/api/recommendations/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("추천 데이터 반환"))
                .andExpect(jsonPath("$.data.status").value("OK"));
    }
}
