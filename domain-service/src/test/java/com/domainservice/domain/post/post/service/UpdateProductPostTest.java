package com.domainservice.domain.post.post.service;

import com.domainservice.common.configuration.feignclient.user.UserClient;
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
import feign.FeignException;
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
import java.util.Optional;

import static com.common.exception.vo.ProductPostExceptionCode.*;
import static com.common.exception.vo.UserExceptionCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @Mock
    private ImageService imageService;

    @Mock
    private UserClient userClient;

    @Mock
    private MultipartFile mockImageFile;

    @DisplayName("판매자는 게시글을 수정할 수 있다.")
    @Test
    void test1() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro 급매!")
                .name("iPhone 15 Pro 256GB")
                .price(1100000)
                .description("가격 인하했습니다")
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .tagIds(Arrays.asList("tag-1", "tag-2"))
                .build();

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

        Image mockImage = Image.builder()
                .s3Url("https://s3.example.com/updated.webp")
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(tagRepository.findAllById(request.tagIds())).willReturn(tags);
        given(imageService.uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT)))
                .willReturn(List.of(mockImage));

        // when
        ProductPostResponse result = productPostService.updateProductPost(request, imageFiles, userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.price()).isEqualTo(request.price());
        assertThat(result.description()).isEqualTo(request.description());
        assertThat(result.status()).isEqualTo(request.status());

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
        verify(tagRepository).findAllById(request.tagIds());
        verify(imageService).uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT));
    }

    @DisplayName("ADMIN은 게시글을 수정할 수 있다.")
    @Test
    void test2() {
        // given
        String userId = "admin-123";
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro 급매!")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        Image mockImage = Image.builder()
                .s3Url("https://s3.example.com/updated.webp")
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createAdmin(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(imageService.uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT)))
                .willReturn(List.of(mockImage));

        // when
        ProductPostResponse result = productPostService.updateProductPost(request, imageFiles, userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
        verify(imageService).uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT));
    }

    @DisplayName("일반 USER는 게시글을 수정할 수 없다.")
    @Test
    void test3() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("수정된 제목")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createUser(userId));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, imageFiles, userId, postId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(SELLER_PERMISSION_REQUIRED.getMessage());

        verify(userClient).getUserById(userId);
        verify(productPostRepository, never()).findById(anyString());
    }

    @DisplayName("존재하지 않는 사용자는 게시글을 수정할 수 없다.")
    @Test
    void test4() {
        // given
        String userId = "invalid-user";
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("수정된 제목")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        given(userClient.getUserById(userId)).willThrow(FeignException.NotFound.class);

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, imageFiles, userId, postId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(USER_NOT_FOUND.getMessage());

        verify(userClient).getUserById(userId);
        verify(productPostRepository, never()).findById(anyString());
    }

    @DisplayName("이미지 없이 수정하면 예외가 발생한다.")
    @Test
    void test5() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro 급매!")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, null, userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(IMAGE_REQUIRED.getMessage());

        assertThatThrownBy(() -> productPostService.updateProductPost(request, Collections.emptyList(), userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(IMAGE_REQUIRED.getMessage());

        verify(userClient, times(2)).getUserById(userId);
        verify(productPostRepository, times(2)).findById(postId);
    }

    @DisplayName("존재하지 않는 게시글은 수정할 수 없다.")
    @Test
    void test6() {
        // given
        String userId = "user-123";
        String postId = "invalid-post-id";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("수정된 제목")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, imageFiles, userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_NOT_FOUND.getMessage());

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
    }

    @DisplayName("인증되지 않은 사용자는 게시글을 수정할 수 없다.")
    @Test
    void test7() {
        // given
        String userId = null;
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("수정된 제목")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(userClient.getUserById(userId)).willThrow(FeignException.Unauthorized.class);

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, imageFiles, userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(EXTERNAL_API_ERROR.getMessage());

        verify(userClient).getUserById(userId);
    }

    @DisplayName("다른 사용자의 게시글은 수정할 수 없다.")
    @Test
    void test8() {
        // given
        String userId = "user-456";
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("수정된 제목")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, imageFiles, userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_FORBIDDEN.getMessage());

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
    }

    @DisplayName("판매 완료된 게시글은 수정할 수 없다.")
    @Test
    void test9() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("수정된 제목")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SOLDOUT)
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, imageFiles, userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(CANNOT_UPDATE_SOLDOUT.getMessage());

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
    }

    @DisplayName("이미 삭제된 게시글은 수정할 수 없다.")
    @Test
    void test10() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("수정된 제목")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

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

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, imageFiles, userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(ALREADY_DELETED.getMessage());

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
    }

    @DisplayName("존재하지 않는 태그가 포함되면 수정할 수 없다.")
    @Test
    void test11() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<String> tagIds = Arrays.asList("tag-1", "tag-2", "invalid-tag");
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro 급매!")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .tagIds(tagIds)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        List<Tag> tags = Arrays.asList(
                Tag.builder().name("급매").build(),
                Tag.builder().name("256GB").build()
        );

        Image mockImage = Image.builder()
                .s3Url("https://s3.example.com/updated.webp")
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(imageService.uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT)))
                .willReturn(List.of(mockImage));
        given(tagRepository.findAllById(tagIds)).willReturn(tags);

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, imageFiles, userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(TAG_NOT_FOUND.getMessage());

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
    }

    @DisplayName("태그를 null로 수정하면 태그가 유지된다.")
    @Test
    void test12() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<MultipartFile> imageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro 급매!")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .tagIds(null)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        Image mockImage = Image.builder()
                .s3Url("https://s3.example.com/updated.webp")
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(imageService.uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT)))
                .willReturn(List.of(mockImage));

        // when
        ProductPostResponse result = productPostService.updateProductPost(request, imageFiles, userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("아이폰 15 Pro 급매!");
        assertThat(result.price()).isEqualTo(1100000);

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
        verify(imageService).uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT));
        verify(tagRepository, never()).findAllById(anyList());
    }

    @DisplayName("이미지가 10개를 초과하면 수정할 수 없다.")
    @Test
    void test13() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<MultipartFile> tooManyImages = Arrays.asList(
                mockImageFile, mockImageFile, mockImageFile, mockImageFile, mockImageFile,
                mockImageFile, mockImageFile, mockImageFile, mockImageFile, mockImageFile,
                mockImageFile
        );

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when & then
        assertThatThrownBy(() -> productPostService.updateProductPost(request, tooManyImages, userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(TOO_MANY_IMAGES.getMessage());

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
        verify(imageService, never()).uploadProfileImageListByTarget(anyList(), any());
    }

    @DisplayName("기존 이미지는 삭제되고 새 이미지로 교체된다.")
    @Test
    void test14() {
        // given
        String userId = "user-123";
        String postId = "post-123";
        List<MultipartFile> newImageFiles = List.of(mockImageFile);

        ProductPostRequest request = ProductPostRequest.builder()
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1100000)
                .categoryId("category-123")
                .status(ProductStatus.GOOD)
                .build();

        ProductPost existingPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        Image newImage = Image.builder()
                .s3Url("https://s3.example.com/new-image.webp")
                .build();

        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        given(productPostRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(imageService.uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT)))
                .willReturn(List.of(newImage));

        // when
        ProductPostResponse result = productPostService.updateProductPost(request, newImageFiles, userId, postId);

        // then
        assertThat(result).isNotNull();

        verify(userClient).getUserById(userId);
        verify(productPostRepository).findById(postId);
        verify(productPostRepository).flush();
        verify(imageService).uploadProfileImageListByTarget(anyList(), eq(ImageTarget.PRODUCT));
    }
}