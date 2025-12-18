package com.aiservice.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.repository.VectorRepository;
import com.aiservice.infrastructure.feign.DomainServiceClient;
import com.aiservice.infrastructure.feign.dto.PageResponse;
import com.aiservice.infrastructure.feign.dto.ProductPostEsSearchResponse;

@ExtendWith(MockitoExtension.class)
class HybridSearchProcessorTest {

        @Mock
        private DomainServiceClient domainServiceClient;

        @Mock
        private VectorRepository vectorRepository;

        @Spy
        private RRFMerger rrfMerger;

        @InjectMocks
        private HybridSearchProcessor hybridSearchProcessor;

        @Nested
        @DisplayName("search 메서드 테스트")
        class SearchTest {

                @Test
                @DisplayName("ES와 Vector 결과를 RRF로 병합하여 반환한다")
                void test1() {
                        // given
                        String query = "아이폰";
                        int limit = 10;

                        List<DocumentSearchResponse> vectorResults = List.of(
                                        createVectorDoc("prod-1", "아이폰 15 Pro", 0.9),
                                        createVectorDoc("prod-2", "아이폰 14", 0.8));
                        List<ProductPostEsSearchResponse> esResults = List.of(
                                        createEsDoc("prod-2", "아이폰 14"),
                                        createEsDoc("prod-3", "아이폰 케이스"));

                        given(vectorRepository.similaritySearch(query, limit)).willReturn(vectorResults);
                        given(domainServiceClient.search(query, limit))
                                        .willReturn(new PageResponse<>(0, 1, limit, false, esResults));

                        // when
                        List<DocumentSearchResponse> results = hybridSearchProcessor.search(query, limit);

                        // then
                        assertThat(results).isNotEmpty();
                        // prod-2는 양쪽에 있으므로 RRF 점수가 가장 높아야 함
                        assertThat(results.getFirst().metadata().get("productId")).isEqualTo("prod-2");
                }

                @Test
                @DisplayName("ES 결과가 없으면 Vector 결과만 반환한다")
                void test2() {
                        // given
                        String query = "아이폰";
                        int limit = 10;

                        List<DocumentSearchResponse> vectorResults = List.of(
                                        createVectorDoc("prod-1", "아이폰 15", 0.9));

                        given(vectorRepository.similaritySearch(query, limit)).willReturn(vectorResults);
                        given(domainServiceClient.search(query, limit))
                                        .willReturn(new PageResponse<>(0, 0, limit, false, List.of()));

                        // when
                        List<DocumentSearchResponse> results = hybridSearchProcessor.search(query, limit);

                        // then
                        assertThat(results).hasSize(1);
                        assertThat(results.get(0).metadata().get("productId")).isEqualTo("prod-1");
                }

                @Test
                @DisplayName("ES 검색이 예외 발생하면 Vector 결과만 반환한다")
                void test3() {
                        // given
                        String query = "아이폰";
                        int limit = 10;

                        List<DocumentSearchResponse> vectorResults = List.of(
                                        createVectorDoc("prod-1", "아이폰 15", 0.9));

                        given(vectorRepository.similaritySearch(query, limit)).willReturn(vectorResults);
                        given(domainServiceClient.search(query, limit))
                                        .willThrow(new RuntimeException("Feign 호출 실패"));

                        // when
                        List<DocumentSearchResponse> results = hybridSearchProcessor.search(query, limit);

                        // then
                        assertThat(results).hasSize(1);
                        assertThat(results.get(0).metadata().get("productId")).isEqualTo("prod-1");
                }

                @Test
                @DisplayName("Vector 결과가 비어있고 ES 결과만 있으면 ES 결과를 반환한다")
                void test4() {
                        // given
                        String query = "아이폰";
                        int limit = 10;

                        List<ProductPostEsSearchResponse> esResults = List.of(
                                        createEsDoc("prod-1", "아이폰 15"),
                                        createEsDoc("prod-2", "아이폰 케이스"));

                        given(vectorRepository.similaritySearch(query, limit)).willReturn(List.of());
                        given(domainServiceClient.search(query, limit))
                                        .willReturn(new PageResponse<>(0, 1, limit, false, esResults));

                        // when
                        List<DocumentSearchResponse> results = hybridSearchProcessor.search(query, limit);

                        // then
                        assertThat(results).hasSize(2);
                }

                @Test
                @DisplayName("limit 개수만큼만 결과를 반환한다")
                void test5() {
                        // given
                        String query = "스마트폰";
                        int limit = 2;

                        List<DocumentSearchResponse> vectorResults = List.of(
                                        createVectorDoc("prod-1", "스마트폰 A", 0.9),
                                        createVectorDoc("prod-2", "스마트폰 B", 0.8),
                                        createVectorDoc("prod-3", "스마트폰 C", 0.7));
                        List<ProductPostEsSearchResponse> esResults = List.of(
                                        createEsDoc("prod-4", "스마트폰 D"),
                                        createEsDoc("prod-5", "스마트폰 E"));

                        given(vectorRepository.similaritySearch(query, limit)).willReturn(vectorResults);
                        given(domainServiceClient.search(query, limit))
                                        .willReturn(new PageResponse<>(0, 1, limit, false, esResults));

                        // when
                        List<DocumentSearchResponse> results = hybridSearchProcessor.search(query, limit);

                        // then
                        assertThat(results).hasSize(2);
                }
        }

        @Nested
        @DisplayName("RRF 점수 계산 테스트")
        class RRFScoreTest {

                @Test
                @DisplayName("양쪽 검색 결과에 동일 상품이 있으면 RRF 점수가 합산된다")
                void test1() {
                        // given
                        String query = "공통상품";
                        int limit = 10;

                        // prod-common은 양쪽에서 1위
                        List<DocumentSearchResponse> vectorResults = List.of(
                                        createVectorDoc("prod-common", "공통 상품", 0.95),
                                        createVectorDoc("prod-vector-only", "벡터 전용", 0.85));
                        List<ProductPostEsSearchResponse> esResults = List.of(
                                        createEsDoc("prod-common", "공통 상품"),
                                        createEsDoc("prod-es-only", "ES 전용"));

                        given(vectorRepository.similaritySearch(query, limit)).willReturn(vectorResults);
                        given(domainServiceClient.search(query, limit))
                                        .willReturn(new PageResponse<>(0, 1, limit, false, esResults));

                        // when
                        List<DocumentSearchResponse> results = hybridSearchProcessor.search(query, limit);

                        // then
                        // prod-common이 양쪽에서 1위이므로 RRF 점수가 가장 높아야 함
                        assertThat(results.getFirst().metadata().get("productId")).isEqualTo("prod-common");
                }
        }

        private DocumentSearchResponse createVectorDoc(String productId, String content, double score) {
                Map<String, Object> metadata = Map.of(
                                "productId", productId,
                                "categoryName", "테스트",
                                "price", 100000);
                return new DocumentSearchResponse(productId, content, metadata, score);
        }

        private ProductPostEsSearchResponse createEsDoc(String id, String title) {
                return ProductPostEsSearchResponse.builder()
                                .id(id)
                                .title(title)
                                .name(title)
                                .description("설명")
                                .categoryName("테스트")
                                .price(100000L)
                                .tags(List.of("테스트"))
                                .build();
        }
}
