package com.aiservice.infrastructure.qdrant;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.ProductVectorContent;
import com.aiservice.domain.repository.VectorRepository;

class VectorRepositoryTest {

    private InMemoryVectorRepository vectorRepository;

    @BeforeEach
    void setUp() {
        vectorRepository = new InMemoryVectorRepository();
    }

    @Nested
    @DisplayName("addDocument 테스트")
    class AddDocumentTest {

        @Test
        @DisplayName("상품 문서를 추가하면 문서 ID를 반환한다")
        void test1() {
            // given
            ProductVectorContent product = createProduct("prod-1", "아이폰 15 Pro", "애플의 최신 스마트폰");

            // when
            String documentId = vectorRepository.addDocument(product);

            // then
            assertThat(documentId).isNotNull().isNotBlank();
            assertThat(vectorRepository.getDocumentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("여러 상품을 추가하면 각각 다른 문서 ID가 생성된다")
        void test2() {
            // given
            ProductVectorContent product1 = createProduct("prod-1", "아이폰 15", "애플 스마트폰");
            ProductVectorContent product2 = createProduct("prod-2", "갤럭시 S24", "삼성 스마트폰");

            // when
            String docId1 = vectorRepository.addDocument(product1);
            String docId2 = vectorRepository.addDocument(product2);

            // then
            assertThat(docId1).isNotEqualTo(docId2);
            assertThat(vectorRepository.getDocumentCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findDocumentByProductId 테스트")
    class FindDocumentByProductIdTest {

        @Test
        @DisplayName("존재하는 productId로 조회하면 문서를 반환한다")
        void test1() {
            // given
            ProductVectorContent product = createProduct("prod-123", "아이폰 15 Pro", "애플의 최신 스마트폰");
            vectorRepository.addDocument(product);

            // when
            Optional<Document> result = vectorRepository.findDocumentByProductId("prod-123");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getMetadata().get("productId")).isEqualTo("prod-123");
        }

        @Test
        @DisplayName("존재하지 않는 productId로 조회하면 빈 Optional을 반환한다")
        void test2() {
            // given
            ProductVectorContent product = createProduct("prod-1", "아이폰 15", "스마트폰");
            vectorRepository.addDocument(product);

            // when
            Optional<Document> result = vectorRepository.findDocumentByProductId("non-existent");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("similaritySearch 테스트")
    class SimilaritySearchTest {

        @Test
        @DisplayName("검색어와 일치하는 문서를 반환한다")
        void test1() {
            // given
            vectorRepository.addDocument(createProduct("prod-1", "아이폰 15 Pro", "애플의 최신 플래그십 스마트폰"));
            vectorRepository.addDocument(createProduct("prod-2", "갤럭시 S24", "삼성의 플래그십 스마트폰"));
            vectorRepository.addDocument(createProduct("prod-3", "에어팟 프로", "애플의 무선 이어폰"));

            // when
            List<DocumentSearchResponse> results = vectorRepository.similaritySearch("아이폰", 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).metadata().get("productId")).isEqualTo("prod-1");
        }

        @Test
        @DisplayName("maxResults만큼만 결과를 반환한다")
        void test2() {
            // given
            vectorRepository.addDocument(createProduct("prod-1", "스마트폰 A", "최신 스마트폰"));
            vectorRepository.addDocument(createProduct("prod-2", "스마트폰 B", "프리미엄 스마트폰"));
            vectorRepository.addDocument(createProduct("prod-3", "스마트폰 C", "가성비 스마트폰"));

            // when
            List<DocumentSearchResponse> results = vectorRepository.similaritySearch("스마트폰", 2);

            // then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("일치하는 문서가 없으면 빈 리스트를 반환한다")
        void test3() {
            // given
            vectorRepository.addDocument(createProduct("prod-1", "아이폰 15", "스마트폰"));

            // when
            List<DocumentSearchResponse> results = vectorRepository.similaritySearch("노트북", 10);

            // then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("검색 결과에 메타데이터가 포함된다")
        void test4() {
            // given
            ProductVectorContent product = ProductVectorContent.builder()
                    .productId("prod-1")
                    .title("아이폰 15 Pro")
                    .description("애플의 최신 스마트폰")
                    .categoryName("모바일")
                    .price(1500000)
                    .tags(List.of("애플", "아이폰", "프리미엄"))
                    .build();
            vectorRepository.addDocument(product);

            // when
            List<DocumentSearchResponse> results = vectorRepository.similaritySearch("아이폰", 10);

            // then
            assertThat(results).hasSize(1);
            DocumentSearchResponse result = results.get(0);
            assertThat(result.metadata().get("productId")).isEqualTo("prod-1");
            assertThat(result.metadata().get("categoryName")).isEqualTo("모바일");
            assertThat(result.metadata().get("price")).isEqualTo(1500000);
        }
    }

    @Nested
    @DisplayName("deleteDocument 테스트")
    class DeleteDocumentTest {

        @Test
        @DisplayName("productId로 문서를 삭제한다")
        void test1() {
            // given
            ProductVectorContent product = createProduct("prod-1", "아이폰 15", "스마트폰");
            vectorRepository.addDocument(product);
            assertThat(vectorRepository.getDocumentCount()).isEqualTo(1);

            // when
            vectorRepository.deleteDocument("prod-1");

            // then
            assertThat(vectorRepository.getDocumentCount()).isEqualTo(0);
            assertThat(vectorRepository.findDocumentByProductId("prod-1")).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 productId를 삭제해도 에러가 발생하지 않는다")
        void test2() {
            // given
            ProductVectorContent product = createProduct("prod-1", "아이폰 15", "스마트폰");
            vectorRepository.addDocument(product);

            // when & then
            assertThatCode(() -> vectorRepository.deleteDocument("non-existent"))
                    .doesNotThrowAnyException();
            assertThat(vectorRepository.getDocumentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("삭제된 문서는 검색되지 않는다")
        void test3() {
            // given
            vectorRepository.addDocument(createProduct("prod-1", "아이폰 15", "스마트폰"));
            vectorRepository.addDocument(createProduct("prod-2", "갤럭시 S24", "스마트폰"));

            // when
            vectorRepository.deleteDocument("prod-1");
            List<DocumentSearchResponse> results = vectorRepository.similaritySearch("아이폰", 10);

            // then
            assertThat(results).isEmpty();
        }
    }

    private ProductVectorContent createProduct(String productId, String title, String description) {
        return ProductVectorContent.builder()
                .productId(productId)
                .title(title)
                .description(description)
                .categoryName("전자기기")
                .price(100000)
                .tags(List.of("테스트"))
                .build();
    }
}
