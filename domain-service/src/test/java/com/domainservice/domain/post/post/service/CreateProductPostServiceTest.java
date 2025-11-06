package com.domainservice.domain.post.post.service;

import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
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
class CreateProductPostServiceTest {

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

        CreateProductPostRequest request = new CreateProductPostRequest(
                "아이폰 15 Pro",
                "iPhone 15 Pro",
                1200000,
                "거의 새것입니다.",
                "category-123",
                ProductStatus.GOOD,
                "https://example.com/image.jpg",
                tagIds
        );

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
        ProductPost result = productPostService.createProductPost(request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTitle()).isEqualTo(request.title());
        assertThat(result.getName()).isEqualTo(request.name());
        assertThat(result.getPrice()).isEqualTo(request.price());
        assertThat(result.getTradeStatus()).isEqualTo(TradeStatus.SELLING);

        verify(tagRepository).findAllById(tagIds);
        verify(productPostRepository).save(any(ProductPost.class));
    }

    @DisplayName("태그 없이도 상품 게시글을 생성할 수 있다.")
    @Test
    void test2() {
        // given
        String userId = "user-123";
        List<String> emptyTagIds = Collections.emptyList();

        CreateProductPostRequest request = new CreateProductPostRequest(
                "아이폰 15 Pro",
                "iPhone 15 Pro",
                1200000,
                "거의 새것입니다.",
                "category-123",
                ProductStatus.GOOD,
                "https://example.com/image.jpg",
                emptyTagIds
        );

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
        ProductPost result = productPostService.createProductPost(request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);

        verify(tagRepository).findAllById(emptyTagIds);
        verify(productPostRepository).save(any(ProductPost.class));
    }

    @DisplayName("존재하지 않는 태그가 포함되면 예외가 발생한다.")
    @Test
    void test3() {
        // given
        String userId = "user-123";
        List<String> tagIds = Arrays.asList("tag-1", "tag-2", "invalid-tag");

        CreateProductPostRequest request = new CreateProductPostRequest(
                "아이폰 15 Pro",
                "iPhone 15 Pro",
                1200000,
                "거의 새것입니다.",
                "category-123",
                ProductStatus.GOOD,
                "https://example.com/image.jpg",
                tagIds
        );

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
}