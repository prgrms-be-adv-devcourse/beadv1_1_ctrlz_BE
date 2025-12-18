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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.aiservice.application.SseCommunicationService;

@WebMvcTest(RecommendationSseController.class)
class RecommendationSseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SseCommunicationService sseCommunicationService;

    @Test
    @DisplayName("SSE 구독 요청 성공")
    void test1() throws Exception {
        // given
        String userId = "user1";
        SseEmitter mockEmitter = new SseEmitter();
        given(sseCommunicationService.connect(userId)).willReturn(mockEmitter);

        // when & then
        mockMvc.perform(get("/sse/recommendations/{userId}", userId))
                .andExpect(status().isOk());
    }
}
