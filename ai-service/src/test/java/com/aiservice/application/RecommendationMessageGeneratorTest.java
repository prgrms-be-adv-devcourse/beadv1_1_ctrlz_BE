package com.aiservice.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.UserContext;

@ExtendWith(MockitoExtension.class)
class RecommendationMessageGeneratorTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private UserContextService userContextService;

    @Mock
    private PromptTemplate promptTemplate;

    @Mock
    private ChatOptions chatOptions;

    @InjectMocks
    private RecommendationMessageGenerator generator;

    @DisplayName("추천 상품이 없을 경우 null 반환")
    @Test
    void test1() {
        // given
        String userId = "user1";
        String query = "test query";
        List<DocumentSearchResponse> emptyDocs = Collections.emptyList();

        // when
        String result = generator.toPrompt(userId, query, emptyDocs);

        // then
        assertThat(result).isNull();
    }

    @DisplayName("정상적인 프롬프트 생성 및 ChatClient 호출 성공")
    @Test
    void test2() {
        // given
        String userId = "user1";
        String query = "노트북 추천해줘";
        DocumentSearchResponse doc = new DocumentSearchResponse(
                "doc1",
                "노트북 설명",
                Map.of("productId", "p1", "price", 1000000L),
                0.9);
        List<DocumentSearchResponse> recommendations = List.of(doc);

        UserContext userContext = UserContext.builder()
                .gender("M")
                .age(30)
                .searchKeywords(List.of("laptop"))
                .cartProductNames(List.of("mouse"))
                .viewedTitle(List.of("monitor"))
                .build();

        when(userContextService.getUserContext(userId)).thenReturn(userContext);
        when(promptTemplate.render(anyMap())).thenReturn("Rendered Prompt");

        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.options(any(ChatOptions.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("AI Response");

        // when
        String result = generator.toPrompt(userId, query, recommendations);

        // then
        assertThat(result).isEqualTo("AI Response");
        verify(userContextService).getUserContext(userId);
        verify(promptTemplate).render(anyMap());
        verify(chatClient.prompt().system("Rendered Prompt").user(query).options(chatOptions)).call();
    }

    @DisplayName("ChatClient 호출 중 예외 발생 시 null 반환")
    @Test
    void test3() {
        // given
        String userId = "user1";
        String query = "error query";
        DocumentSearchResponse doc = new DocumentSearchResponse(
                "doc1",
                "desc",
                Map.of("productId", "p1", "price", 100L),
                0.5);

        when(userContextService.getUserContext(userId)).thenReturn(UserContext.builder()
                .searchKeywords(Collections.emptyList())
                .cartProductNames(Collections.emptyList())
                .viewedTitle(Collections.emptyList())
                .build());
        when(promptTemplate.render(anyMap())).thenReturn("Prompt");

        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.options(any(ChatOptions.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("AI Error"));

        // when
        String result = generator.toPrompt(userId, query, List.of(doc));

        // then
        assertThat(result).isNull();
    }
}
