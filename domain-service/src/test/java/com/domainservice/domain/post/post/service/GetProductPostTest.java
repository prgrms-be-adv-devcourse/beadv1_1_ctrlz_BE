package com.domainservice.domain.post.post.service;

import com.common.exception.CustomException;
import com.domainservice.common.configuration.feignclient.user.UserClient;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static com.common.exception.vo.ProductPostExceptionCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ProductPostService 조회 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
class GetProductPostTest {

    @InjectMocks
    private ProductPostService productPostService;

    @Mock
    private ProductPostRepository productPostRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private RecentlyViewedService recentlyViewedService;

    private static final int MAX_COUNT = 10;

    private void setViewCount(ProductPost productPost, int viewCount) throws Exception {
        Field field = productPost.getClass().getDeclaredField("viewCount");
        field.setAccessible(true);
        field.set(productPost, viewCount);
    }

    private int getViewCount(ProductPost productPost) throws Exception {
        Field field = productPost.getClass().getDeclaredField("viewCount");
        field.setAccessible(true);
        return (int) field.get(productPost);
    }

    private void setId(ProductPost productPost, String id) throws Exception {
        Field field = productPost.getClass().getSuperclass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(productPost, id);
    }

    @DisplayName("익명 사용자는 게시글을 조회할 수 있다.")
    @Test
    void test1() throws Exception {
        // given
        String userId = "anonymous";
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
        ProductPostResponse result = productPostService.getProductPostById(userId, postId);

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
        verify(userClient, never()).getUserById(anyString());
        verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
    }

    @DisplayName("익명 사용자 조회 시 조회수가 증가한다.")
    @Test
    void test2() throws Exception {
        // given
        String userId = "anonymous";
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
        productPostService.getProductPostById(userId, postId);

        // then
        int currentViewCount = getViewCount(productPost);
        assertThat(currentViewCount).isEqualTo(initialViewCount + 1);

        verify(productPostRepository).findById(postId);
        verify(userClient, never()).getUserById(anyString());
        verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
    }

    @DisplayName("인증된 사용자는 게시글을 조회하고 최근 본 상품에 저장된다.")
    @Test
    void test3() throws Exception {
        // given
        String userId = "user-456";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("갤럭시 S24 Ultra")
                .name("Galaxy S24 Ultra")
                .price(1500000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setId(productPost, postId);
        setViewCount(productPost, 20);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createUser(userId));
        doNothing().when(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);

        // when
        ProductPostResponse result = productPostService.getProductPostById(userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("갤럭시 S24 Ultra");

        verify(productPostRepository).findById(postId);
        verify(userClient).getUserById(userId);
        verify(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);
    }

    @DisplayName("존재하지 않는 사용자가 조회하면 CustomException이 발생한다.")
    @Test
    void test4() throws Exception {
        // given
        String userId = "nonexistent-user";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("맥북 프로 16")
                .name("MacBook Pro 16")
                .price(3000000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setViewCount(productPost, 15);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
        given(userClient.getUserById(userId)).willThrow(FeignException.NotFound.class);

        // when & then
        assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
                .isInstanceOf(CustomException.class);

        verify(productPostRepository).findById(postId);
        verify(userClient).getUserById(userId);
        verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
    }

    @DisplayName("User Service 인증 실패 시 ProductPostException이 발생한다.")
    @Test
    void test5() throws Exception {
        // given
        String userId = "user-789";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("에어팟 프로 2세대")
                .name("AirPods Pro 2nd")
                .price(350000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setViewCount(productPost, 25);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
        given(userClient.getUserById(userId)).willThrow(FeignException.Unauthorized.class);

        // when & then
        assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(EXTERNAL_API_ERROR.getMessage());

        verify(productPostRepository).findById(postId);
        verify(userClient).getUserById(userId);
        verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
    }

    @DisplayName("User Service 권한 없음 시 ProductPostException이 발생한다.")
    @Test
    void test6() throws Exception {
        // given
        String userId = "user-999";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("아이패드 프로")
                .name("iPad Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setViewCount(productPost, 30);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
        given(userClient.getUserById(userId)).willThrow(FeignException.Forbidden.class);

        // when & then
        assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(EXTERNAL_API_ERROR.getMessage());

        verify(productPostRepository).findById(postId);
        verify(userClient).getUserById(userId);
        verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
    }

    @DisplayName("User Service 통신 오류 시 ProductPostException이 발생한다.")
    @Test
    void test7() throws Exception {
        // given
        String userId = "user-111";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-123")
                .title("애플워치")
                .name("Apple Watch")
                .price(500000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setViewCount(productPost, 40);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
        given(userClient.getUserById(userId)).willThrow(mock(FeignException.class));

        // when & then
        assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(EXTERNAL_API_ERROR.getMessage());

        verify(productPostRepository).findById(postId);
        verify(userClient).getUserById(userId);
        verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
    }

    @DisplayName("존재하지 않는 게시글은 조회할 수 없다.")
    @Test
    void test8() {
        // given
        String userId = "user-123";
        String postId = "invalid-post-id";

        given(productPostRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_NOT_FOUND.getMessage());

        verify(productPostRepository).findById(postId);
        verify(userClient, never()).getUserById(anyString());
    }

    @DisplayName("삭제된 게시글은 조회할 수 없다.")
    @Test
    void test9() {
        // given
        String userId = "user-123";
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
        assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
                .isInstanceOf(ProductPostException.class)
                .hasMessage(PRODUCT_POST_DELETED.getMessage());

        verify(productPostRepository).findById(postId);
        verify(userClient, never()).getUserById(anyString());
    }

    @DisplayName("판매 완료된 게시글도 조회할 수 있다.")
    @Test
    void test10() throws Exception {
        // given
        String userId = "anonymous";
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
        ProductPostResponse result = productPostService.getProductPostById(userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.SOLDOUT);

        verify(productPostRepository).findById(postId);
        verify(userClient, never()).getUserById(anyString());
    }

    @DisplayName("거래 진행 중인 게시글도 조회할 수 있다.")
    @Test
    void test11() throws Exception {
        // given
        String userId = "anonymous";
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
        ProductPostResponse result = productPostService.getProductPostById(userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.PROCESSING);

        verify(productPostRepository).findById(postId);
        verify(userClient, never()).getUserById(anyString());
    }

    @DisplayName("인증된 사용자가 판매 완료 게시글을 조회하면 최근 본 상품에 저장된다.")
    @Test
    void test12() throws Exception {
        // given
        String userId = "user-789";
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

        setId(productPost, postId);
        setViewCount(productPost, 100);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createUser(userId));
        doNothing().when(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);

        // when
        ProductPostResponse result = productPostService.getProductPostById(userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.SOLDOUT);

        verify(productPostRepository).findById(postId);
        verify(userClient).getUserById(userId);
        verify(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);
    }

    @DisplayName("SELLER 권한 사용자도 게시글을 조회할 수 있다.")
    @Test
    void test13() throws Exception {
        // given
        String userId = "seller-123";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-456")
                .categoryId("category-123")
                .title("맥북 에어 M2")
                .name("MacBook Air M2")
                .price(1500000)
                .status(ProductStatus.NEW)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setId(productPost, postId);
        setViewCount(productPost, 5);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createSeller(userId));
        doNothing().when(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);

        // when
        ProductPostResponse result = productPostService.getProductPostById(userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("맥북 에어 M2");

        verify(productPostRepository).findById(postId);
        verify(userClient).getUserById(userId);
        verify(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);
    }

    @DisplayName("ADMIN 권한 사용자도 게시글을 조회할 수 있다.")
    @Test
    void test14() throws Exception {
        // given
        String userId = "admin-123";
        String postId = "post-123";

        ProductPost productPost = ProductPost.builder()
                .userId("user-789")
                .categoryId("category-123")
                .title("닌텐도 스위치")
                .name("Nintendo Switch")
                .price(350000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        setId(productPost, postId);
        setViewCount(productPost, 50);

        given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
        given(userClient.getUserById(userId)).willReturn(UserViewFactory.createAdmin(userId));
        doNothing().when(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);

        // when
        ProductPostResponse result = productPostService.getProductPostById(userId, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("닌텐도 스위치");

        verify(productPostRepository).findById(postId);
        verify(userClient).getUserById(userId);
        verify(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);
    }
}