package com.domainservice.domain.post.favorite.service;

import static com.common.exception.vo.ProductPostExceptionCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.common.model.web.PageResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoritePostResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoriteProductResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoriteStatusResponse;
import com.domainservice.domain.post.favorite.model.entity.FavoriteProduct;
import com.domainservice.domain.post.favorite.repository.FavoriteRepository;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.repository.ProductPostRepository;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

	@InjectMocks
	private FavoriteService favoriteService;

	@Mock
	private FavoriteRepository favoriteRepository;

	@Mock
	private ProductPostRepository productPostRepository;

	/**
	 * 리플렉션을 사용하여 BaseEntity를 상속받은 엔티티의 id 필드(부모 필드)를 강제로 설정하는 유틸 메서드
	 * @param entity ID를 설정할 엔티티 객체
	 * @param id 설정할 ID 값
	 */
	private void setEntityId(Object entity, String id) {
		try {
			Class<?> clazz = entity.getClass();
			while (clazz != null) {
				try {
					Field field = clazz.getDeclaredField("id");
					field.setAccessible(true);
					field.set(entity, id);
					return;
				} catch (NoSuchFieldException e) {
					clazz = clazz.getSuperclass();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("ID 설정 실패", e);
		}
	}

	@DisplayName("사용자는 상품을 찜 목록에 추가할 수 있다.")
	@Test
	void test1() {
		// given
		String userId = "user-123";
		String productPostId = "product-123";

		ProductPost productPost = ProductPost.builder()
			.title("맥북 프로 16인치")
			.userId("seller-1")
			.build();
		setEntityId(productPost, productPostId);

		given(productPostRepository.findById(productPostId)).willReturn(Optional.of(productPost));
		given(favoriteRepository.saveAndFlush(any(FavoriteProduct.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		FavoritePostResponse result = favoriteService.addFavoriteProduct(userId, productPostId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isFavorite()).isTrue();

		verify(productPostRepository).findById(productPostId);
		verify(favoriteRepository).saveAndFlush(any(FavoriteProduct.class));
	}

	@DisplayName("이미 찜한 상품을 다시 누르면 찜 목록에서 삭제된다.")
	@Test
	void test2() {
		// given
		String userId = "user-123";
		String productPostId = "product-123";

		ProductPost productPost = ProductPost.builder()
			.title("맥북 프로 16인치")
			.userId("seller-1")
			.build();
		setEntityId(productPost, productPostId);
		productPost.incrementLikedCount();

		FavoriteProduct favoriteProduct = FavoriteProduct.builder()
			.userId("user-123")
			.productPost(productPost)
			.build();

		given(favoriteRepository
			.findByUserIdAndProductPostId(anyString(), anyString())).willReturn(Optional.of(favoriteProduct));

		// when
		FavoritePostResponse result = favoriteService.cancelFavoriteProduct(userId, productPostId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isFavorite()).isFalse();

		verify(favoriteRepository).delete(favoriteProduct);
	}


	@DisplayName("찜하지 않은 상품을 취소하려고 하면 예외가 발생한다.")
	@Test
	void test3() {
		// given
		String userId = "user-123";
		String productPostId = "product-123";

		given(favoriteRepository.findByUserIdAndProductPostId(userId, productPostId))
			.willReturn(Optional.empty());

		// when & then
		ProductPostException exception = assertThrows(
			ProductPostException.class,
			() -> favoriteService.cancelFavoriteProduct(userId, productPostId)
		);

		assertThat(exception.getCode()).isEqualTo(FAVORITE_NOT_FOUND.getCode());

		// 조회 메서드는 호출됨
		verify(favoriteRepository).findByUserIdAndProductPostId(userId, productPostId);

		verify(favoriteRepository, never()).saveAndFlush(any(FavoriteProduct.class));
		verify(favoriteRepository, never()).delete(any(FavoriteProduct.class));
	}

	@DisplayName("존재하지 않는 상품을 찜하려고 하면 예외가 발생한다.")
	@Test
	void test4() {
		// given
		String userId = "user-123";
		String invalidProductId = "invalid-product";

		given(productPostRepository.findById(invalidProductId)).willReturn(Optional.empty());

		// when & then
		ProductPostException exception = assertThrows(
			ProductPostException.class,
			() -> favoriteService.addFavoriteProduct(userId, invalidProductId)
		);

		assertThat(exception.getCode()).isEqualTo(PRODUCT_POST_NOT_FOUND.getCode());
	}

	@DisplayName("사용자는 내가 찜한 상품 목록을 조회할 수 있다.")
	@Test
	void test5() {
		// given
		String userId = "user-123";
		Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());

		ProductPost productPost = ProductPost.builder()
			.title("테스트 상품")
			.price(10000)
			.build();
		setEntityId(productPost, "product-1");

		FavoriteProduct favoriteProduct = FavoriteProduct.builder()
			.userId(userId)
			.productPost(productPost)
			.build();
		setEntityId(favoriteProduct, "fav-1");

		List<FavoriteProduct> favoriteList = List.of(favoriteProduct);
		Page<FavoriteProduct> favoritePage = new PageImpl<>(favoriteList, pageable, 1);

		given(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).willReturn(favoritePage);

		// when
		PageResponse<List<FavoriteProductResponse>> result = favoriteService.getMyFavoriteProductList(userId, pageable);

		// then
		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0).title()).isEqualTo("테스트 상품");
		assertThat(result.totalPages()).isEqualTo(1);

		verify(favoriteRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
	}

	@DisplayName("특정 상품의 찜 여부를 조회할 수 있다.")
	@Test
	void test6() {
		// given
		String userId = "user-123";
		String productPostId = "product-123";

		given(favoriteRepository.existsByUserIdAndProductPostId(userId, productPostId)).willReturn(true);

		// when
		FavoriteStatusResponse result = favoriteService.isFavorite(userId, productPostId);

		// then
		assertThat(result.isWishListed()).isTrue();
		verify(favoriteRepository).existsByUserIdAndProductPostId(userId, productPostId);
	}

}
