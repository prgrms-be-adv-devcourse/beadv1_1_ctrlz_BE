package com.aiservice.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.RecommendationResult;
import com.aiservice.domain.vo.RecommendationStatus;

@ExtendWith(MockitoExtension.class)
class ProductRecommendationServiceTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private HybridSearchProcessor hybridSearchProcessor;

    @Mock
    private RecommendationMessageGenerator messageGenerator;

    @InjectMocks
    private ProductRecommendationService productRecommendationService;

    @Nested
    @DisplayName("recommendProductsByQuery 테스트")
    class RecommendProductsByQueryTest {

        @Test
        @DisplayName("추천 제한에 도달하면 LIMIT_REACHED 결과를 발행한다")
        void test1() {
            // given
            String userId = "user-1";
            String query = "아이폰";
            ReflectionTestUtils.setField(productRecommendationService, "recommendationLimit", 3);

            given(sessionService.getRecommendationCount(userId)).willReturn(3);

            // when
            productRecommendationService.recommendProductsByQuery(userId, query);

            // then
            ArgumentCaptor<RecommendationResult> captor = ArgumentCaptor.forClass(RecommendationResult.class);
            then(sessionService).should().publishRecommendationData(eq(userId), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(RecommendationStatus.LIMIT_REACHED);
            then(hybridSearchProcessor).should(never()).search(anyString(), anyInt());
        }

        @Test
        @DisplayName("검색 결과가 있고 메시지 생성 성공하면 OK 결과를 발행한다")
        void test2() {
            // given
            String userId = "user-1";
            String query = "아이폰";
            ReflectionTestUtils.setField(productRecommendationService, "recommendationLimit", 10);

            List<DocumentSearchResponse> searchResults = List.of(
                    createDocSearchResponse("prod-1", 1500000));

            given(sessionService.getRecommendationCount(userId)).willReturn(0);
            given(hybridSearchProcessor.search(query, 20)).willReturn(searchResults);
            given(messageGenerator.toPrompt(userId, query, searchResults))
                    .willReturn("추천 메시지입니다.");

            // when
            productRecommendationService.recommendProductsByQuery(userId, query);

            // then
            ArgumentCaptor<RecommendationResult> captor = ArgumentCaptor.forClass(RecommendationResult.class);
            then(sessionService).should().publishRecommendationData(eq(userId), captor.capture());

            RecommendationResult result = captor.getValue();
            assertThat(result.status()).isEqualTo(RecommendationStatus.OK);
            assertThat(result.message()).isEqualTo("추천 메시지입니다.");

            then(sessionService).should().incrementRecommendationCount(userId);
        }

        @Test
        @DisplayName("메시지 생성이 null을 반환하면 NO_RESULTS 결과를 발행한다")
        void test3() {
            // given
            String userId = "user-1";
            String query = "아이폰";
            ReflectionTestUtils.setField(productRecommendationService, "recommendationLimit", 10);

            List<DocumentSearchResponse> searchResults = List.of(
                    createDocSearchResponse("prod-1", 1500000));

            given(sessionService.getRecommendationCount(userId)).willReturn(0);
            given(hybridSearchProcessor.search(query, 20)).willReturn(searchResults);
            given(messageGenerator.toPrompt(userId, query, searchResults)).willReturn(null);

            // when
            productRecommendationService.recommendProductsByQuery(userId, query);

            // then
            ArgumentCaptor<RecommendationResult> captor = ArgumentCaptor.forClass(RecommendationResult.class);
            then(sessionService).should().publishRecommendationData(eq(userId), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(RecommendationStatus.NO_RESULTS);
        }

        @Test
        @DisplayName("검색 결과가 없으면 NO_RESULTS 결과를 발행한다")
        void test4() {
            // given
            String userId = "user-1";
            String query = "존재하지않는상품";
            ReflectionTestUtils.setField(productRecommendationService, "recommendationLimit", 10);

            given(sessionService.getRecommendationCount(userId)).willReturn(0);
            given(hybridSearchProcessor.search(query, 20)).willReturn(List.of());
            given(messageGenerator.toPrompt(userId, query, List.of())).willReturn(null);

            // when
            productRecommendationService.recommendProductsByQuery(userId, query);

            // then
            ArgumentCaptor<RecommendationResult> captor = ArgumentCaptor.forClass(RecommendationResult.class);
            then(sessionService).should().publishRecommendationData(eq(userId), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(RecommendationStatus.NO_RESULTS);
        }
    }

    private DocumentSearchResponse createDocSearchResponse(String productId, int price) {
        Map<String, Object> metadata = Map.of(
                "productId", productId,
                "price", price);
        return new DocumentSearchResponse(productId, "상품 설명", metadata, 0.9);
    }
}
