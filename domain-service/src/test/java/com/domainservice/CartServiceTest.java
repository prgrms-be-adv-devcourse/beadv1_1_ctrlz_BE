package com.domainservice;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.domainservice.domain.cart.model.dto.response.CartItemResponse;
import com.domainservice.domain.cart.model.entity.Cart;
import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.repository.CartItemJpaRepository;
import com.domainservice.domain.cart.repository.CartJpaRepository;
import com.domainservice.domain.cart.service.CartService;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.service.ProductPostService;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private CartJpaRepository cartJpaRepository;

	@Mock
	private CartItemJpaRepository cartItemJpaRepository;

	@Mock
	private ProductPostService productPostService;
	@InjectMocks
	private CartService cartService;

	private String userId;
	private Cart cart;
	private ProductPostResponse productPostResponse;

	@BeforeEach
	void setUp() {
		userId = "testUser";
		cart = Cart.builder()
			.userId(userId)
			.cartItems(new ArrayList<>())
			.build();
		productPostResponse = new ProductPostResponse(
			"p1",
			"seller1",
			"category1",
			"테스트 상품",
			"테스트 이름",
			1000,
			"테스트 설명",
			null,
			null,
			List.of(),
			null,
			List.of(),
			LocalDateTime.now(),
			LocalDateTime.now()
		);
	}

	@Test
	@DisplayName("사용자 ID로 장바구니 조회 - 존재하는 경우")
	void test1() {
		// given
		when(cartJpaRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

		// when
		Cart result = cartService.getCartByUserId(userId);

		// then
		assertThat(result).isEqualTo(cart);
		verify(cartJpaRepository).findByUserId(userId);
	}

	@Test
	@DisplayName("장바구니 아이템 목록 조회")
	void test3() {
		// given
		List<CartItem> cartItems = new ArrayList<>();
		CartItem item = CartItem.builder()
			.productPostId("p1")
			.cart(cart)
			.selected(true)
			.build();
		cartItems.add(item);

		when(cartJpaRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
		when(cartItemJpaRepository.findByCart(cart)).thenReturn(cartItems);
		when(productPostService.getProductPostById("p1")).thenReturn(productPostResponse);
		// when
		List<CartItemResponse> result = cartService.getCartItemList(userId);

		// then
		assertThat(result).hasSize(1);
		verify(cartJpaRepository).findByUserId(userId);
		verify(cartItemJpaRepository).findByCart(cart);
	}

	@Test
	@DisplayName("장바구니에 새 아이템 추가")
	void test4() {
		// given
		CartItem newItem = CartItem.builder()
			.productPostId("p1")
			.cart(cart)
			.selected(true)
			.build();

		when(cartJpaRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
		when(cartJpaRepository.save(any(Cart.class))).thenReturn(cart);
		when(productPostService.getProductPostById("p1")).thenReturn(productPostResponse);

		// when
		CartItemResponse result = cartService.addItem(userId, "p1", 2);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isSelected()).isTrue();
		verify(cartJpaRepository).findByUserId(userId);
		verify(cartJpaRepository).save(cart);
	}

	@Test
	@DisplayName("장바구니 아이템 선택 상태 변경")
	void test7() {
		// given
		String itemId = "item123";
		CartItem cartItem = CartItem.builder()
			.productPostId("p1")
			.cart(cart)
			.selected(true)
			.build();

		when(cartItemJpaRepository.findById(itemId)).thenReturn(Optional.of(cartItem));
		when(cartItemJpaRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));
		when(productPostService.getProductPostById("p1")).thenReturn(productPostResponse);

		// when
		CartItemResponse result = cartService.setItemSelected(itemId, false);

		// then
		assertThat(result.isSelected()).isFalse();
		verify(cartItemJpaRepository).findById(itemId);
		verify(cartItemJpaRepository).save(cartItem);
	}

	@Test
	@DisplayName("장바구니에서 아이템 삭제")
	void test8() {
		// given
		String itemId = "item123";

		CartItem item = CartItem.builder()
			.cart(cart)
			.selected(true)
			.build();

		cart.addCartItem(item);

		when(cartItemJpaRepository.findById(itemId)).thenReturn(Optional.of(item));

		// when
		cartService.removeItem(itemId);

		// then
		verify(cartItemJpaRepository).findById(itemId);
		verify(cartItemJpaRepository).delete(item);
		verify(cartJpaRepository).save(cart);
	}
}