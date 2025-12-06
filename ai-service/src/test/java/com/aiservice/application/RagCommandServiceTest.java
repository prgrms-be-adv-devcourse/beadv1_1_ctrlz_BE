package com.aiservice.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import com.aiservice.application.command.CreateProductVectorCommand;
import com.aiservice.domain.repository.VectorRepository;

@ExtendWith(MockitoExtension.class)
class RagCommandServiceTest {

    @Mock
    private VectorRepository vectorRepository;

    @InjectMocks
    private RagCommandService ragCommandService;

    @Nested
    @DisplayName("uploadData 테스트")
    class UploadDataTest {

        @Test
        @DisplayName("새 상품을 업로드하면 문서 ID를 반환한다")
        void test1() {
            // given
            CreateProductVectorCommand command = createCommand("prod-1", "아이폰 15");
            given(vectorRepository.findDocumentByProductId("prod-1")).willReturn(Optional.empty());
            given(vectorRepository.addDocument(any())).willReturn("doc-123");

            // when
            String result = ragCommandService.uploadData(command);

            // then
            assertThat(result).isEqualTo("doc-123");
            then(vectorRepository).should().addDocument(any());
            then(vectorRepository).should(never()).deleteDocument(anyString());
        }

        @Test
        @DisplayName("기존 상품이 있으면 삭제 후 업로드한다")
        void test2() {
            // given
            CreateProductVectorCommand command = createCommand("prod-1", "아이폰 15");
            Document existingDoc = new Document("existing-doc-id", "content", java.util.Map.of());

            given(vectorRepository.findDocumentByProductId("prod-1")).willReturn(Optional.of(existingDoc));
            given(vectorRepository.addDocument(any())).willReturn("new-doc-123");

            // when
            String result = ragCommandService.uploadData(command);

            // then
            assertThat(result).isEqualTo("new-doc-123");
            then(vectorRepository).should().deleteDocument("prod-1");
            then(vectorRepository).should().addDocument(any());
        }

        @Test
        @DisplayName("상품 정보가 올바르게 변환되어 저장된다")
        void test3() {
            // given
            CreateProductVectorCommand command = new CreateProductVectorCommand(
                    "prod-123",
                    "아이폰 15 Pro",
                    "iPhone 15 Pro",
                    "모바일",
                    "판매중",
                    1500000,
                    "애플 최신 스마트폰",
                    List.of("애플", "아이폰", "프리미엄"));

            given(vectorRepository.findDocumentByProductId("prod-123")).willReturn(Optional.empty());
            given(vectorRepository.addDocument(argThat(content -> content.productId().equals("prod-123") &&
                    content.title().equals("아이폰 15 Pro") &&
                    content.categoryName().equals("모바일") &&
                    content.price() == 1500000))).willReturn("doc-456");

            // when
            String result = ragCommandService.uploadData(command);

            // then
            assertThat(result).isEqualTo("doc-456");
        }
    }

    @Nested
    @DisplayName("deleteData 테스트")
    class DeleteDataTest {

        @Test
        @DisplayName("productId로 문서를 삭제한다")
        void test1() {
            // given
            String productId = "prod-1";

            // when
            ragCommandService.deleteData(productId);

            // then
            then(vectorRepository).should().deleteDocument(productId);
        }

        @Test
        @DisplayName("존재하지 않는 productId를 삭제해도 예외가 발생하지 않는다")
        void test2() {
            // given
            String productId = "non-existent";

            // when & then
            assertThatCode(() -> ragCommandService.deleteData(productId))
                    .doesNotThrowAnyException();
            then(vectorRepository).should().deleteDocument(productId);
        }
    }

    @Nested
    @DisplayName("CreateProductVectorCommand 검증 테스트")
    class CommandValidationTest {

        @Test
        @DisplayName("productId가 null이면 예외가 발생한다")
        void test1() {
            // when & then
            assertThatThrownBy(() -> new CreateProductVectorCommand(
                    null, "제목", "이름", "카테고리", "상태", 1000, "설명", List.of())).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("productId");
        }

        @Test
        @DisplayName("productId가 빈 문자열이면 예외가 발생한다")
        void test2() {
            // when & then
            assertThatThrownBy(() -> new CreateProductVectorCommand(
                    "", "제목", "이름", "카테고리", "상태", 1000, "설명", List.of())).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("productId");
        }

        @Test
        @DisplayName("price가 0 이하면 예외가 발생한다")
        void test3() {
            // when & then
            assertThatThrownBy(() -> new CreateProductVectorCommand(
                    "prod-1", "제목", "이름", "카테고리", "상태", 0, "설명", List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("price");
        }

        @Test
        @DisplayName("null 필드들은 기본값으로 설정된다")
        void test4() {
            // when
            CreateProductVectorCommand command = new CreateProductVectorCommand(
                    "prod-1", null, null, null, null, 1000, null, null);

            // then
            assertThat(command.title()).isEmpty();
            assertThat(command.name()).isEmpty();
            assertThat(command.categoryName()).isEmpty();
            assertThat(command.status()).isEmpty();
            assertThat(command.description()).isEmpty();
            assertThat(command.tags()).isEmpty();
        }
    }

    private CreateProductVectorCommand createCommand(String productId, String title) {
        return new CreateProductVectorCommand(
                productId,
                title,
                title,
                "테스트",
                "판매중",
                100000,
                "설명",
                List.of("테스트"));
    }
}
