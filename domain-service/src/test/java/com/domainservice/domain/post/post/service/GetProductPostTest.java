package com.domainservice.domain.post.post.service;

import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
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
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * ProductPostService 조회 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
class GetProductPostTest {

    @InjectMocks
    private ProductPostService productPostService;

    @Mock
    private ProductPostRepository productPostRepository;

    /**
     * 테스트용 헬퍼 메서드 - viewCount를 조작하기 위해 사용
     */
    private void setViewCount(ProductPost productPost, int viewCount) throws Exception {
        Field field = productPost.getClass().getDeclaredField("viewCount");
        field.setAccessible(true);
        field.set(productPost, viewCount);
    }

    /**
     * 테스트용 헬퍼 메서드 - viewCount를 조회하기 위해 사용
     */
    private int getViewCount(ProductPost productPost) throws Exception {
        Field field = productPost.getClass().getDeclaredField("viewCount");
        field.setAccessible(true);
        return (int) field.get(productPost);
    }

    @DisplayName("게시글을 조회할 수 있다.")
    @Test
    void test1() throws Exception {
        // given
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .description("거의 새것입니다")
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setViewCount(productPost, 10);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));

        // when
        ProductPostResponse result = productPostService.getProductPostById(postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo("user-123");
        assertThat(result.title()).isEqualTo("아이폰 15 Pro");
        assertThat(result.name()).isEqualTo("iPhone 15 Pro");
        assertThat(result.price()).isEqualTo(1200000);
        assertThat(result.description()).isEqualTo("거의 새것입니다");
        assertThat(result.status()).isEqualTo(ProductStatus.GOOD);
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.SELLING);

        verify(productPostRepository).findById(postId);
    }

    @DisplayName("게시글 조회 시 조회수가 증가한다.")
    @Test
    void test2() throws Exception {
        // given
        String postId = "post-123";
        int initialViewCount = 100;

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setViewCount(productPost, initialViewCount);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));

        // when
        productPostService.getProductPostById(postId);

        // then
        int currentViewCount = getViewCount(productPost);
        assertThat(currentViewCount).isEqualTo(initialViewCount + 1);

        verify(productPostRepository).findById(postId);
    }

    @DisplayName("존재하지 않는 게시글은 조회할 수 없다.")
    @Test
    void test3() {
        // given
        String postId = "invalid-post-id";

        given(productPostRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productPostService.getProductPostById(postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_NOT_FOUND.getMessage());
    }

    @DisplayName("삭제된 게시글은 조회할 수 없다.")
    @Test
    void test4() {
        // given
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

        productPost.delete();

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));

        // when & then
        assertThatThrownBy(() -> productPostService.getProductPostById(postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_DELETED.getMessage());
    }

    @DisplayName("판매 완료된 게시글도 조회할 수 있다.")
    @Test
    void test5() throws Exception {
        // given
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro (판매완료)")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SOLDOUT)
                .build();

        setViewCount(productPost, 50);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));

        // when
        ProductPostResponse result = productPostService.getProductPostById(postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.SOLDOUT);

        verify(productPostRepository).findById(postId);
    }

    @DisplayName("거래 진행 중인 게시글도 조회할 수 있다.")
    @Test
    void test6() throws Exception {
        // given
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이폰 15 Pro (거래중)")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.PROCESSING)
                .build();

        setViewCount(productPost, 30);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));

        // when
        ProductPostResponse result = productPostService.getProductPostById(postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.PROCESSING);

        verify(productPostRepository).findById(postId);
    }

    @DisplayName("조회수가 0인 게시글을 조회할 수 있다.")
    @Test
    void test7() throws Exception {
        // given
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("새로 등록한 아이폰")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.NEW)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setViewCount(productPost, 0);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));

        // when
        ProductPostResponse result = productPostService.getProductPostById(postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ProductStatus.NEW);

        // 조회수 증가 확인
        int currentViewCount = getViewCount(productPost);
        assertThat(currentViewCount).isEqualTo(1);

        verify(productPostRepository).findById(postId);
    }

}
