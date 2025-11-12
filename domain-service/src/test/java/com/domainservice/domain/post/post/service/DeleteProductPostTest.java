package com.domainservice.domain.post.post.service;

import com.common.model.persistence.BaseEntity.DeleteStatus;
import com.domainservice.domain.asset.image.application.ImageService;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.entity.ProductPostImage;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ProductPostService 삭제 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
class DeleteProductPostTest {

    @InjectMocks
    private ProductPostService productPostService;

    @Mock
    private ProductPostRepository productPostRepository;

    @Mock
    private ImageService imageService;

    /**
     * Entity의 ID를 설정하기 위해 사용
     */
    private void setId(ProductPost productPost, String id) throws Exception {
        Field field = productPost.getClass().getSuperclass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(productPost, id);
    }

    /**
     * Image의 ID를 설정하기 위해 사용
     */
    private void setIdForImage(Image image, String id) throws Exception {
        Field field = image.getClass().getSuperclass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(image, id);
    }

    @DisplayName("게시글을 삭제할 수 있다.")
    @Test
    void test1() throws Exception {
        // given
        String userId = "user-123";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setId(productPost, postId);

        given(productPostRepository.findById(postId))
                .willReturn(Optional.of(productPost));

        // when
        String deletedId = productPostService.deleteProductPost(userId, postId);

        // then
        assertThat(deletedId).isEqualTo(postId);
        assertThat(productPost.getDeleteStatus()).isEqualTo(DeleteStatus.D);
        verify(productPostRepository).findById(postId);
        verify(productPostRepository).flush();  // 이미지 삭제 시 flush 호출
    }

    @DisplayName("이미지가 있는 게시글을 삭제하면 이미지도 함께 삭제된다.")
    @Test
    void test2() throws Exception {
        // given
        String userId = "user-123";
        String postId = "post-123";
        String imageId = "image-123";

        Image image = Image.builder()
                .s3Url("https://s3.example.com/image.webp")
                .build();

        // Image에도 ID 설정 필요!
        setIdForImage(image, imageId);

        ProductPost productPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        // 이미지 추가
        productPost.addImages(java.util.List.of(image));
        setId(productPost, postId);

        given(productPostRepository.findById(postId))
                .willReturn(Optional.of(productPost));
        doNothing().when(imageService).deleteProfileImageById(imageId);

        // when
        String deletedId = productPostService.deleteProductPost(userId, postId);

        // then
        assertThat(deletedId).isEqualTo(postId);
        assertThat(productPost.getDeleteStatus()).isEqualTo(DeleteStatus.D);
        verify(productPostRepository).findById(postId);
        verify(productPostRepository).flush();
        verify(imageService).deleteProfileImageById(imageId);
    }

    @DisplayName("존재하지 않는 게시글은 삭제할 수 없다.")
    @Test
    void test3() {
        // given
        String userId = "user-123";
        String postId = "invalid-post-id";

        given(productPostRepository.findById(postId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productPostService.deleteProductPost(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_NOT_FOUND.getMessage());
    }

    @DisplayName("인증되지 않은 사용자는 게시글을 삭제할 수 없다.")
    @Test
    void test4() {
        // given
        String userId = null;
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId))
                .willReturn(Optional.of(productPost));

        // when & then
        assertThatThrownBy(() -> productPostService.deleteProductPost(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(UNAUTHORIZED.getMessage());
    }

    @DisplayName("빈 문자열 userId로는 게시글을 삭제할 수 없다.")
    @Test
    void test5() {
        // given
        String userId = "";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId))
                .willReturn(Optional.of(productPost));

        // when & then
        assertThatThrownBy(() -> productPostService.deleteProductPost(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(UNAUTHORIZED.getMessage());
    }

    @DisplayName("다른 사용자의 게시글은 삭제할 수 없다.")
    @Test
    void test6() {
        // given
        String userId = "user-456";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        given(productPostRepository.findById(postId))
                .willReturn(Optional.of(productPost));

        // when & then
        assertThatThrownBy(() -> productPostService.deleteProductPost(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_FORBIDDEN.getMessage());
    }

    @DisplayName("거래 진행 중인 게시글은 삭제할 수 없다.")
    @Test
    void test7() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.PROCESSING)
                .build();

        given(productPostRepository.findById(postId))
                .willReturn(Optional.of(productPost));

        // when & then
        assertThatThrownBy(() -> productPostService.deleteProductPost(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_IN_PROGRESS.getMessage());
    }

    @DisplayName("이미 삭제된 게시글은 다시 삭제할 수 없다.")
    @Test
    void test8() {
        // given
        String userId = "user-123";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId(userId)
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        productPost.delete();

        given(productPostRepository.findById(postId))
                .willReturn(Optional.of(productPost));

        // when & then
        assertThatThrownBy(() -> productPostService.deleteProductPost(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(ALREADY_DELETED.getMessage());
    }
}
