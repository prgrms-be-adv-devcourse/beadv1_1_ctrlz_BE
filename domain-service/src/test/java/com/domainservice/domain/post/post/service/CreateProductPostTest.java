package com.domainservice.domain.post.post.service;

import com.domainservice.domain.asset.image.application.ImageService;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.asset.image.domain.entity.ImageTarget;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.request.ProductPostRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @Mock
    private ImageService imageService;

    @Mock
    private MultipartFile mockImageFile;

    @DisplayName("상품 게시글을 생성할 수 있다.")
    @Test
    void test1() {
        // given
        String userId = "user-123";
        List<String> tagIds = Arrays.asList("tag-1", "tag-2");
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .description("거의 새것입니다.")
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .tagIds(tagIds)
                .build();

        List<Tag> tags = Arrays.asList(
                Tag.builder().name("거의새것").build(),
                Tag.builder().name("256GB").build()
        );

        Image mockImage = Image.builder()
                .s3Url("https://s3.example.com/image.webp")
                .build();

        ProductPost savedProductPost = ProductPost.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title())
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .status(request.status())
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(tagRepository.findAllById(tagIds)).willReturn(tags);
        given(imageService.uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT)))
                .willReturn(List.of(mockImage));
        given(productPostRepository.save(any(ProductPost.class))).willReturn(savedProductPost);

        // when
        ProductPostResponse result = productPostService.createProductPost(request, userId, imageFiles);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.price()).isEqualTo(request.price());
        assertThat(result.description()).isEqualTo(request.description());
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.SELLING);

        verify(tagRepository).findAllById(tagIds);
        verify(imageService).uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT));
        verify(productPostRepository).save(any(ProductPost.class));
    }

    @DisplayName("태그 없이도 상품 게시글을 생성할 수 있다.")
    @Test
    void test2() {
        // given
        String userId = "user-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .description("거의 새것입니다.")
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .tagIds(null)  // 태그 없음
                .build();

        Image mockImage = Image.builder()
                .s3Url("https://s3.example.com/image.webp")
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

        given(imageService.uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT)))
                .willReturn(List.of(mockImage));
        given(productPostRepository.save(any(ProductPost.class))).willReturn(savedProductPost);

        // when
        ProductPostResponse result = productPostService.createProductPost(request, userId, imageFiles);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.title()).isEqualTo(request.title());

        verify(imageService).uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT));
        verify(productPostRepository).save(any(ProductPost.class));
        verify(tagRepository, never()).findAllById(anyList());
    }

    @DisplayName("존재하지 않는 태그가 포함되면 예외가 발생한다.")
    @Test
    void test3() {
        // given
        String userId = "user-123";
        List<String> tagIds = Arrays.asList("tag-1", "tag-2", "invalid-tag");
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .description("거의 새것입니다.")
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .tagIds(tagIds)
                .build();

        // 요청한 태그 3개 중 2개만 조회됨
        List<Tag> tags = Arrays.asList(
                Tag.builder().name("거의새것").build(),
                Tag.builder().name("256GB").build()
        );

        given(tagRepository.findAllById(tagIds)).willReturn(tags);

        // when & then
        assertThatThrownBy(() -> productPostService.createProductPost(request, userId, imageFiles))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(TAG_NOT_FOUND.getMessage());

        verify(tagRepository).findAllById(tagIds);
        verify(imageService, never()).uploadProfileImageListByTarget(anyList(), any());
        verify(productPostRepository, never()).save(any());
    }

    @DisplayName("빈 태그 리스트로 상품 게시글을 생성할 수 있다.")
    @Test
    void test4() {
        // given
        String userId = "user-123";
        List<String> emptyTagIds = Collections.emptyList();
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .tagIds(emptyTagIds)
                .build();

        Image mockImage = Image.builder()
                .s3Url("https://s3.example.com/image.webp")
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
        given(imageService.uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT)))
                .willReturn(List.of(mockImage));
        given(productPostRepository.save(any(ProductPost.class))).willReturn(savedProductPost);

        // when
        ProductPostResponse result = productPostService.createProductPost(request, userId, imageFiles);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);

        verify(tagRepository).findAllById(emptyTagIds);
        verify(imageService).uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT));
        verify(productPostRepository).save(any(ProductPost.class));
    }

    @DisplayName("이미지 없이 게시글을 생성하면 예외가 발생한다.")
    @Test
    void test5() {
        // given
        String userId = "user-123";

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        // when & then
        assertThatThrownBy(() -> productPostService.createProductPost(request, userId, null))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(IMAGE_REQUIRED.getMessage());

        assertThatThrownBy(() -> productPostService.createProductPost(request, userId, Collections.emptyList()))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(IMAGE_REQUIRED.getMessage());

        verify(imageService, never()).uploadProfileImageListByTarget(anyList(), any());
        verify(productPostRepository, never()).save(any());
    }

    @DisplayName("이미지가 10개를 초과하면 예외가 발생한다.")
    @Test
    void test6() {
        // given
        String userId = "user-123";
        List<MultipartFile> tooManyImages = Arrays.asList(
                mockImageFile, mockImageFile, mockImageFile, mockImageFile, mockImageFile,
                mockImageFile, mockImageFile, mockImageFile, mockImageFile, mockImageFile,
                mockImageFile  // 11개
        );

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        // when & then
        assertThatThrownBy(() -> productPostService.createProductPost(request, userId, tooManyImages))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(TOO_MANY_IMAGES.getMessage());

        verify(imageService, never()).uploadProfileImageListByTarget(anyList(), any());
        verify(productPostRepository, never()).save(any());
    }

}
