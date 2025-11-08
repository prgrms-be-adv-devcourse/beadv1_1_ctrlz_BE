package com.domainservice.domain.post.post.service;

import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
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

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * ProductPostService 생성 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
class CreateProductPostTest {

    @InjectMocks
    private ProductPostService productPostService;

    @Mock
    private ProductPostRepository productPostRepository;

    @Mock
    private TagRepository tagRepository;

    @DisplayName("상품 게시글을 생성할 수 있다.")
    @Test
    void test1() {
        // given
        String userId = "user-123";
        List<String> tagIds = Arrays.asList("tag-1", "tag-2");

        CreateProductPostRequest request = CreateProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .description("거의 새것입니다.")
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .imageUrl("https://example.com/image.jpg")
                .tagIds(tagIds)
                .build();

        List<Tag> tags = Arrays.asList(
                Tag.builder().name("거의새것").build(),
                Tag.builder().name("256GB").build()
        );

        ProductPost savedProductPost = ProductPost.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title())
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .status(request.status())
                .tradeStatus(TradeStatus.SELLING)
                .imageUrl(request.imageUrl())
                .build();

        given(tagRepository.findAllById(tagIds)).willReturn(tags);
        given(productPostRepository.save(any(ProductPost.class))).willReturn(savedProductPost);

        // when
        ProductPostResponse result = productPostService.createProductPost(request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.price()).isEqualTo(request.price());
        assertThat(result.description()).isEqualTo(request.description());
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.SELLING);

        verify(tagRepository).findAllById(tagIds);
        verify(productPostRepository).save(any(ProductPost.class));
    }

    @DisplayName("태그 없이도 상품 게시글을 생성할 수 있다.")
    @Test
    void test2() {
        // given
        String userId = "user-123";

        CreateProductPostRequest request = CreateProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .description("거의 새것입니다.")
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .imageUrl("https://example.com/image.jpg")
                .tagIds(null)  // 태그 없음
                .build();

        ProductPost savedProductPost = ProductPost.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title())
                .name(request.name())
                .price(request.price())
                .status(request.status())
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.save(any(ProductPost.class))).willReturn(savedProductPost);

        // when
        ProductPostResponse result = productPostService.createProductPost(request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.title()).isEqualTo(request.title());

        verify(productPostRepository).save(any(ProductPost.class));
    }

    @DisplayName("존재하지 않는 태그가 포함되면 예외가 발생한다.")
    @Test
    void test3() {
        // given
        String userId = "user-123";
        List<String> tagIds = Arrays.asList("tag-1", "tag-2", "invalid-tag");

        CreateProductPostRequest request = CreateProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .description("거의 새것입니다.")
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .imageUrl("https://example.com/image.jpg")
                .tagIds(tagIds)
                .build();

        // 요청한 태그 3개 중 2개만 조회됨
        List<Tag> tags = Arrays.asList(
                Tag.builder().name("거의새것").build(),
                Tag.builder().name("256GB").build()
        );

        given(tagRepository.findAllById(tagIds)).willReturn(tags);

        // when & then
        assertThatThrownBy(() -> productPostService.createProductPost(request, userId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(TAG_NOT_FOUND.getMessage());

        verify(tagRepository).findAllById(tagIds);
    }

    @DisplayName("빈 태그 리스트로 상품 게시글을 생성할 수 있다.")
    @Test
    void test4() {
        // given
        String userId = "user-123";
        List<String> emptyTagIds = Collections.emptyList();

        CreateProductPostRequest request = CreateProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .tagIds(emptyTagIds)
                .build();

        ProductPost savedProductPost = ProductPost.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title())
                .name(request.name())
                .price(request.price())
                .status(request.status())
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(tagRepository.findAllById(emptyTagIds)).willReturn(Collections.emptyList());
        given(productPostRepository.save(any(ProductPost.class))).willReturn(savedProductPost);

        // when
        ProductPostResponse result = productPostService.createProductPost(request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.tags()).isEmpty();

        verify(tagRepository).findAllById(emptyTagIds);
        verify(productPostRepository).save(any(ProductPost.class));
    }

    @DisplayName("필수 정보만으로 상품 게시글을 생성할 수 있다.")
    @Test
    void test5() {
        // given
        String userId = "user-123";

        CreateProductPostRequest request = CreateProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .description(null)
                .imageUrl(null)
                .tagIds(null)
                .build();

        ProductPost savedProductPost = ProductPost.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title())
                .name(request.name())
                .price(request.price())
                .status(request.status())
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.save(any(ProductPost.class))).willReturn(savedProductPost);

        // when
        ProductPostResponse result = productPostService.createProductPost(request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.description()).isNull();
        assertThat(result.imageUrl()).isNull();

        verify(productPostRepository).save(any(ProductPost.class));
    }
}