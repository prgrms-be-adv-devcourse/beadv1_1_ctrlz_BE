package com.domainservice.domain.post.post.service;

import static com.common.exception.vo.ProductPostExceptionCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;
import com.domainservice.common.configuration.feign.client.UserFeignClient;
import com.domainservice.common.configuration.feign.exception.UserClientException;
import com.domainservice.domain.post.category.model.entity.Category;
import com.domainservice.domain.post.category.repository.CategoryRepository;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.response.ProductPostDescription;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.repository.ProductPostRepository;

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
	private CategoryRepository categoryRepository;

	@Mock
	private UserFeignClient userClient;

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

	private Category createCategory(String categoryName) {
		return Category.builder()
			.name(categoryName)
			.build();
	}

	@DisplayName("익명 사용자는 게시글을 조회할 수 있다.")
	@Test
	void test1() throws Exception {
		// given
		String userId = "anonymous";
		String postId = "post-123";
		String sellerId = "user-123";
		String categoryId = "category-123";

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("아이폰 15 Pro")
			.name("iPhone 15 Pro")
			.price(1200000)
			.description("거의 새것입니다")
			.status(ProductStatus.GOOD)
			.tradeStatus(TradeStatus.SELLING)
			.build();

		setViewCount(productPost, 10);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.of(createCategory("전자기기")));
		given(userClient.getUser(sellerId)).willReturn(UserViewFactory.createUser(sellerId));

		// when
		ProductPostDescription result = productPostService.getProductPostById(userId, postId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.title()).isEqualTo("아이폰 15 Pro");
		assertThat(result.name()).isEqualTo("iPhone 15 Pro");
		assertThat(result.price()).isEqualTo(1200000);
		assertThat(result.description()).isEqualTo("거의 새것입니다");
		assertThat(result.status()).isEqualTo(ProductStatus.GOOD);
		assertThat(result.tradeStatus()).isEqualTo(TradeStatus.SELLING);

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient).getUser(sellerId); // 판매자 정보 조회는 항상 발생
		verify(userClient, never()).getUser(userId); // 익명 사용자는 조회하지 않음
		verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
	}

	@DisplayName("익명 사용자 조회 시 조회수가 증가한다.")
	@Test
	void test2() throws Exception {
		// given
		String userId = "anonymous";
		String postId = "post-123";
		String sellerId = "user-123";
		String categoryId = "category-123";
		int initialViewCount = 100;

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("아이폰 15 Pro")
			.name("iPhone 15 Pro")
			.price(1200000)
			.status(ProductStatus.GOOD)
			.tradeStatus(TradeStatus.SELLING)
			.build();

		setViewCount(productPost, initialViewCount);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.of(createCategory("전자기기")));
		given(userClient.getUser(sellerId)).willReturn(UserViewFactory.createUser(sellerId));

		// when
		productPostService.getProductPostById(userId, postId);

		// then
		int currentViewCount = getViewCount(productPost);
		assertThat(currentViewCount).isEqualTo(initialViewCount + 1);

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient).getUser(sellerId); // 판매자 정보 조회
		verify(userClient, never()).getUser(userId); // 익명 사용자는 조회하지 않음
		verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
	}

	@DisplayName("인증된 사용자는 게시글을 조회하고 최근 본 상품에 저장된다.")
	@Test
	void test3() throws Exception {
		// given
		String userId = "user-456";
		String postId = "post-123";
		String sellerId = "user-123";
		String categoryId = "category-123";

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("갤럭시 S24 Ultra")
			.name("Galaxy S24 Ultra")
			.price(1500000)
			.status(ProductStatus.GOOD)
			.tradeStatus(TradeStatus.SELLING)
			.build();

		setId(productPost, postId);
		setViewCount(productPost, 20);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.of(createCategory("전자기기")));
		given(userClient.getUser(sellerId)).willReturn(UserViewFactory.createUser(sellerId));
		given(userClient.getUser(userId)).willReturn(UserViewFactory.createUser(userId));
		doNothing().when(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);

		// when
		ProductPostDescription result = productPostService.getProductPostById(userId, postId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.title()).isEqualTo("갤럭시 S24 Ultra");

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient).getUser(sellerId); // 판매자 정보 조회
		verify(userClient).getUser(userId); // 조회자 유효성 확인
		verify(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);
	}

	@DisplayName("존재하지 않는 사용자가 조회하면 예외가 발생한다.")
	@Test
	void test4() throws Exception {
		// given
		String userId = "nonexistent-user";
		String postId = "post-123";
		String sellerId = "user-123";
		String categoryId = "category-123";

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("맥북 프로 16")
			.name("MacBook Pro 16")
			.price(3000000)
			.status(ProductStatus.GOOD)
			.tradeStatus(TradeStatus.SELLING)
			.build();

		setViewCount(productPost, 15);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.of(createCategory("전자기기")));
		given(userClient.getUser(sellerId)).willReturn(UserViewFactory.createUser(sellerId));
		given(userClient.getUser(userId)).willThrow(new UserClientException.NotFound("Not Found"));

		// when & then
		assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
			.isInstanceOf(UserClientException.NotFound.class);

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient).getUser(sellerId); // 판매자 정보는 먼저 조회됨
		verify(userClient).getUser(userId); // 조회자 확인 시 예외 발생
		verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
	}

	@DisplayName("User Service 인증 실패 시 예외가 발생한다.")
	@Test
	void test5() throws Exception {
		// given
		String userId = "user-789";
		String postId = "post-123";
		String sellerId = "user-123";
		String categoryId = "category-123";

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("에어팟 프로 2세대")
			.name("AirPods Pro 2nd")
			.price(350000)
			.status(ProductStatus.GOOD)
			.tradeStatus(TradeStatus.SELLING)
			.build();

		setViewCount(productPost, 25);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.of(createCategory("전자기기")));
		given(userClient.getUser(sellerId)).willReturn(UserViewFactory.createUser(sellerId));
		given(userClient.getUser(userId)).willThrow(new UserClientException.Unauthorized("Unauthorized"));

		// when & then
		assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
			.isInstanceOf(UserClientException.Unauthorized.class);

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient).getUser(sellerId); // 판매자 정보는 먼저 조회됨
		verify(userClient).getUser(userId); // 조회자 인증 실패
		verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
	}

	@DisplayName("삭제된 게시글은 조회할 수 없다.")
	@Test
	void test6() {
		// given
		String userId = "user-123";
		String postId = "post-123";
		String categoryId = "category-123";

		ProductPost productPost = ProductPost.builder()
			.userId("user-123")
			.categoryId(categoryId)
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
		verify(categoryRepository, never()).findById(anyString());
		verify(userClient, never()).getUser(anyString());
	}

	@DisplayName("판매 완료된 게시글도 조회할 수 있다.")
	@Test
	void test7() throws Exception {
		// given
		String userId = "anonymous";
		String postId = "post-123";
		String sellerId = "user-123";
		String categoryId = "category-123";

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("아이폰 15 Pro (판매완료)")
			.name("iPhone 15 Pro")
			.price(1200000)
			.status(ProductStatus.GOOD)
			.tradeStatus(TradeStatus.SOLDOUT)
			.build();

		setViewCount(productPost, 50);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.of(createCategory("전자기기")));
		given(userClient.getUser(sellerId)).willReturn(UserViewFactory.createUser(sellerId));

		// when
		ProductPostDescription result = productPostService.getProductPostById(userId, postId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.tradeStatus()).isEqualTo(TradeStatus.SOLDOUT);

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient).getUser(sellerId); // 판매자 정보 조회
		verify(userClient, never()).getUser(userId); // 익명 사용자는 조회하지 않음
	}

	@DisplayName("거래 진행 중인 게시글도 조회할 수 있다.")
	@Test
	void test8() throws Exception {
		// given
		String userId = "anonymous";
		String postId = "post-123";
		String sellerId = "user-123";
		String categoryId = "category-123";

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("아이폰 15 Pro (거래중)")
			.name("iPhone 15 Pro")
			.price(1200000)
			.status(ProductStatus.GOOD)
			.tradeStatus(TradeStatus.PROCESSING)
			.build();

		setViewCount(productPost, 30);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.of(createCategory("전자기기")));
		given(userClient.getUser(sellerId)).willReturn(UserViewFactory.createUser(sellerId));

		// when
		ProductPostDescription result = productPostService.getProductPostById(userId, postId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.tradeStatus()).isEqualTo(TradeStatus.PROCESSING);

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient).getUser(sellerId); // 판매자 정보 조회
		verify(userClient, never()).getUser(userId); // 익명 사용자는 조회하지 않음
	}

	@DisplayName("SELLER 권한 사용자도 게시글을 조회할 수 있다.")
	@Test
	void test9() throws Exception {
		// given
		String userId = "seller-123";
		String postId = "post-123";
		String sellerId = "user-456";
		String categoryId = "category-123";

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("맥북 에어 M2")
			.name("MacBook Air M2")
			.price(1500000)
			.status(ProductStatus.NEW)
			.tradeStatus(TradeStatus.SELLING)
			.build();

		setId(productPost, postId);
		setViewCount(productPost, 5);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.of(createCategory("전자기기")));
		given(userClient.getUser(sellerId)).willReturn(UserViewFactory.createUser(sellerId));
		given(userClient.getUser(userId)).willReturn(UserViewFactory.createSeller(userId));
		doNothing().when(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);

		// when
		ProductPostDescription result = productPostService.getProductPostById(userId, postId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.title()).isEqualTo("맥북 에어 M2");

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient).getUser(sellerId); // 판매자 정보 조회
		verify(userClient).getUser(userId); // 조회자 유효성 확인
		verify(recentlyViewedService).addRecentlyViewedPost(userId, postId, MAX_COUNT);
	}

	@DisplayName("존재하지 않는 카테고리의 게시글 조회 시 예외가 발생한다.")
	@Test
	void test10() throws Exception {
		// given
		String userId = "anonymous";
		String postId = "post-123";
		String sellerId = "user-123";
		String categoryId = "nonexistent-category";

		ProductPost productPost = ProductPost.builder()
			.userId(sellerId)
			.categoryId(categoryId)
			.title("아이폰 15 Pro")
			.name("iPhone 15 Pro")
			.price(1200000)
			.status(ProductStatus.GOOD)
			.tradeStatus(TradeStatus.SELLING)
			.build();

		setViewCount(productPost, 10);

		given(productPostRepository.findById(postId)).willReturn(Optional.of(productPost));
		given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productPostService.getProductPostById(userId, postId))
			.isInstanceOf(ProductPostException.class)
			.hasMessage(CATEGORY_NOT_FOUND.getMessage());

		verify(productPostRepository).findById(postId);
		verify(categoryRepository).findById(categoryId);
		verify(userClient, never()).getUser(anyString());
		verify(recentlyViewedService, never()).addRecentlyViewedPost(anyString(), anyString(), anyInt());
	}

}