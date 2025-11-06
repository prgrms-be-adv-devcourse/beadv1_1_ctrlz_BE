package com.domainservice;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.domainservice.domain.cart.model.entity.Cart;
import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.repository.CartItemJpaRepository;
import com.domainservice.domain.cart.repository.CartJpaRepository;
import com.domainservice.domain.cart.service.CartService;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private CartJpaRepository cartJpaRepository;

	@Mock
	private CartItemJpaRepository cartItemJpaRepository;

	@InjectMocks
	private CartService cartService;

	private String userId;
	private String productPostId;
	private Cart cart;

	@BeforeEach
	void setUp() {
		userId = "testUser";
		productPostId = "test123"; // CartService에서 사용하는 테스트 ID와 일치하도록 설정
		cart = Cart.builder()
			.userId(userId)
			.cartItems(new ArrayList<>())
			.build();
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
	@DisplayName("사용자 ID로 장바구니 조회 - 존재하지 않는 경우 신규 생성")
	void test2() {
		// given
		when(cartJpaRepository.findByUserId(userId)).thenReturn(Optional.empty());
		when(cartJpaRepository.save(any(Cart.class))).thenReturn(cart);

		// when
		Cart result = cartService.getCartByUserId(userId);

		// then
		assertThat(result).isEqualTo(cart);
		verify(cartJpaRepository).findByUserId(userId);
		verify(cartJpaRepository).save(any(Cart.class));
	}

	@Test
	@DisplayName("장바구니 아이템 목록 조회")
	void test3() {
		// given
		List<CartItem> cartItems = new ArrayList<>();
		CartItem item = CartItem.builder()
			.cart(cart)
			.quantity(1)
			.selected(true)
			.build();
		cartItems.add(item);

		when(cartJpaRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
		when(cartItemJpaRepository.findByCart(cart)).thenReturn(cartItems);

		// when
		List<CartItem> result = cartService.getCartItemList(userId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result).isEqualTo(cartItems);
		verify(cartJpaRepository).findByUserId(userId);
		verify(cartItemJpaRepository).findByCart(cart);
	}

	@Test
	@DisplayName("장바구니에 새 아이템 추가")
	void test4() {
		// given
		String newProductId = "newProduct123"; // 존재하지 않는 ID로 설정
		when(cartJpaRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
		when(cartJpaRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		CartItem result = cartService.addItem(userId, newProductId, 2);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getQuantity()).isEqualTo(2);
		assertThat(result.isSelected()).isTrue();
		verify(cartJpaRepository).findByUserId(userId);
		verify(cartJpaRepository).save(cart);
	}

	@Test
	@DisplayName("장바구니에 기존 아이템 수량 증가")
	void test5() {
		// given
		int initialQuantity = 1;
		int addQuantity = 2;

		// 기존 장바구니와 아이템 설정
		CartItem existingItem = CartItem.builder()
			.cart(cart)
			.quantity(initialQuantity)
			.selected(true)
			.build();
		cart.addCartItem(existingItem);

		when(cartJpaRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
		when(cartJpaRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		CartItem result = cartService.addItem(userId, productPostId, addQuantity);

		// then
		ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
		verify(cartJpaRepository).save(captor.capture());
		assertThat(captor.getValue().getCartItems()).hasSize(1);
		assertThat(captor.getValue().getCartItems().get(0).getQuantity())
			.isEqualTo(initialQuantity + addQuantity);
		assertThat(result).isNotNull();
		assertThat(result.getQuantity()).isEqualTo(initialQuantity + addQuantity);
		verify(cartJpaRepository).findByUserId(userId);
	}

	@Test
	@DisplayName("장바구니 아이템 수량 변경")
	void test6() {
		// given
		String itemId = "item123";
		int newQuantity = 5;

		CartItem item = CartItem.builder()
			.cart(cart)
			.quantity(2)
			.selected(true)
			.build();

		when(cartItemJpaRepository.findById(itemId)).thenReturn(Optional.of(item));
		when(cartItemJpaRepository.save(any(CartItem.class))).thenReturn(item);

		// when
		CartItem result = cartService.updateQuantity(itemId, newQuantity);

		// then
		assertThat(result).isEqualTo(item);
		assertThat(result.getQuantity()).isEqualTo(newQuantity);
		verify(cartItemJpaRepository).findById(itemId);
		verify(cartItemJpaRepository).save(item);
	}

	@Test
	@DisplayName("장바구니 아이템 선택 상태 변경")
	void test7() {
		// given
		String itemId = "item123";
		boolean selected = false;

		CartItem item = CartItem.builder()
			.cart(cart)
			.quantity(2)
			.selected(true)
			.build();

		when(cartItemJpaRepository.findById(itemId)).thenReturn(Optional.of(item));
		when(cartItemJpaRepository.save(any(CartItem.class))).thenReturn(item);

		// when
		CartItem result = cartService.setItemSelected(itemId, selected);

		// then
		assertThat(result).isEqualTo(item);
		assertThat(result.isSelected()).isEqualTo(selected);
		verify(cartItemJpaRepository).findById(itemId);
		verify(cartItemJpaRepository).save(item);
	}

	@Test
	@DisplayName("장바구니에서 아이템 삭제")
	void test8() {
		// given
		String itemId = "item123";

		CartItem item = CartItem.builder()
			.cart(cart)
			.quantity(2)
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