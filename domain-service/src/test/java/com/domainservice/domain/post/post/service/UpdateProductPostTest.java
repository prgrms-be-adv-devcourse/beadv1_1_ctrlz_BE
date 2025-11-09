package com.domainservice.domain.post.post.service;

import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.request.UpdateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * ProductPostService 수정 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
class UpdateProductPostTest {

    @InjectMocks
    private ProductPostService productPostService;

    @Mock
    private ProductPostRepository productPostRepository;

    @Mock
    private TagRepository tagRepository;

    @DisplayName("게시글을 수정할 수 있다.")
    @Test
    void test1() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "아이폰 15 Pro 급매!",
                "iPhone 15 Pro 256GB",
                1100000,
                "가격 인하했습니다",
                ProductStatus.GOOD,
                "https://example.com/new-image.jpg",
                Arrays.asList("tag-1", "tag-2")
        );

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.NEW)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        List<Tag> tags = Arrays.asList(
                Tag.builder().name("급매").build(),
                Tag.builder().name("256GB").build()
        );

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(tagRepository.findAllById(request.tagIds())).willReturn(tags);

        // when
        ProductPostResponse result = productPostService.updateProductPost(userId, postId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.price()).isEqualTo(request.price());
        assertThat(result.description()).isEqualTo(request.description());
        assertThat(result.status()).isEqualTo(request.status());

        verify(productPostRepository).findById(postId);
        verify(tagRepository).findAllById(request.tagIds());
    }

    @DisplayName("제목만 수정할 수 있다.")
    @Test
    void test2() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "아이폰 15 Pro 급매!",
                null,
                null,
                null,
                null,
                null,
                null
        );

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .description("거의 새것")
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when
        ProductPostResponse result = productPostService.updateProductPost(userId, postId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("아이폰 15 Pro 급매!");
        assertThat(result.name()).isEqualTo("iPhone 15 Pro");  // 변경 안 됨
        assertThat(result.price()).isEqualTo(1200000);  // 변경 안 됨

        verify(productPostRepository).findById(postId);
    }

    @DisplayName("가격과 설명을 수정할 수 있다.")
    @Test
    void test3() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                null,
                null,
                1100000,
                "가격 인하했습니다",
                null,
                null,
                null
        );

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when
        ProductPostResponse result = productPostService.updateProductPost(userId, postId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.price()).isEqualTo(1100000);
        assertThat(result.description()).isEqualTo("가격 인하했습니다");
        assertThat(result.title()).isEqualTo("아이폰 15 Pro");  // 변경 안 됨

        verify(productPostRepository).findById(postId);
    }

    @DisplayName("존재하지 않는 게시글은 수정할 수 없다.")
    @Test
    void test4() {
        // given
        String userId = "user-123";
        String postId = "invalid-post-id";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "수정된 제목",
                null,
                null,
                null,
                null,
                null,
                null
        );

        given(productPostRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(userId, postId, request))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_NOT_FOUND.getMessage());
    }

    @DisplayName("인증되지 않은 사용자는 게시글을 수정할 수 없다.")
    @Test
    void test5() {
        // given
        String userId = null;
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "수정된 제목",
                null,
                null,
                null,
                null,
                null,
                null
        );

        ProductPost existingPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(userId, postId, request))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(UNAUTHORIZED.getMessage());
    }

    @DisplayName("다른 사용자의 게시글은 수정할 수 없다.")
    @Test
    void test6() {
        // given
        String userId = "user-456";
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "수정된 제목",
                null,
                null,
                null,
                null,
                null,
                null
        );

        ProductPost existingPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(userId, postId, request))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_FORBIDDEN.getMessage());
    }

    @DisplayName("판매 완료된 게시글은 수정할 수 없다.")
    @Test
    void test7() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "수정된 제목",
                null,
                null,
                null,
                null,
                null,
                null
        );

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SOLDOUT)
                .build();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(userId, postId, request))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(CANNOT_UPDATE_SOLDOUT.getMessage());
    }

    @DisplayName("이미 삭제된 게시글은 수정할 수 없다.")
    @Test
    void test8() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "수정된 제목",
                null,
                null,
                null,
                null,
                null,
                null
        );

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        existingPost.delete();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(userId, postId, request))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(ALREADY_DELETED.getMessage());
    }

    @DisplayName("존재하지 않는 태그가 포함되면 수정할 수 없다.")
    @Test
    void test9() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<String> tagIds = Arrays.asList("tag-1", "tag-2", "invalid-tag");

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "아이폰 15 Pro 급매!",
                null,
                null,
                null,
                null,
                null,
                tagIds
        );

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        // 요청한 태그 3개 중 2개만 조회됨
        List<Tag> tags = Arrays.asList(
                Tag.builder().name("급매").build(),
                Tag.builder().name("256GB").build()
        );

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(tagRepository.findAllById(tagIds)).willReturn(tags);

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(userId, postId, request))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(TAG_NOT_FOUND.getMessage());
    }

    @DisplayName("빈 태그 리스트로 수정하면 모든 태그가 삭제된다.")
    @Test
    void test10() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList()
        );

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(tagRepository.findAllById(Collections.emptyList())).willReturn(Collections.emptyList());

        // when
        ProductPostResponse result = productPostService.updateProductPost(userId, postId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.tags()).isEmpty();

        verify(productPostRepository).findById(postId);
        verify(tagRepository).findAllById(Collections.emptyList());
    }

    @DisplayName("태그를 null로 수정하면 태그가 유지된다.")
    @Test
    void test11() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        UpdateProductPostRequest request = new UpdateProductPostRequest(
                "아이폰 15 Pro 급매!",
                null,
                1100000,
                null,
                null,
                null,
                null  // 태그 수정 안 함
        );

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when
        ProductPostResponse result = productPostService.updateProductPost(userId, postId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("아이폰 15 Pro 급매!");
        assertThat(result.price()).isEqualTo(1100000);

        verify(productPostRepository).findById(postId);
    }
}