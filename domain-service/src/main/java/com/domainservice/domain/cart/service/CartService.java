package com.domainservice.domain.cart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.CartExceptionCode;
import com.domainservice.domain.cart.model.dto.response.CartItemResponse;
import com.domainservice.domain.cart.model.entity.Cart;
import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.repository.CartItemJpaRepository;
import com.domainservice.domain.cart.repository.CartJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
	private final CartJpaRepository cartJpaRepository;
	private final CartItemJpaRepository cartItemJpaRepository;

	// TODO 회원가입 프로듀서 전송 시 생성 로직 추가 필요!!

	/**
	 * 1. userid로 장바구니 조회 - 없으면 생성 <p></p>
	 * 2. 장바구니id로 장바구니 아이템 리스트 조회
	 */
	public List<CartItemResponse> getCartItemList(String userId) {

		Cart cart = getCartByUserId(userId);

		return cartItemJpaRepository.findByCart(cart).stream()
			.map(x -> new CartItemResponse("title", "name", x.getTotalPrice(), x.getQuantity(), x.isSelected()))
			.toList();
	}

	public Cart getCartByUserId(String userId) {
		return cartJpaRepository.findByUserId(userId)
			.orElseGet(() -> cartJpaRepository.save(Cart.builder().userId(userId).build()));
	}

	/**
	 * 장바구니 아이템 추가 <p>
	 * 사용자 정보로 장바구니 조회 <p>
	 * 장바구니 아이템을 순회하며 이미 추가된 아이템인지 조회 <p>
	 * 추가된 아이템이면 수량만 업데이트 <p>
	 */
	public CartItemResponse addItem(String userId, String productPostId, int quantity) {
		Cart cart = getCartByUserId(userId);
		List<CartItem> cartItems = cart.getCartItems();

		CartItem targetItem = null;

		String testProductId = "test123";
		for (CartItem item : cartItems) {
			// TODO: 실제 Product 엔티티가 구현되면 아래 로직으로 수정 필요
			if (productPostId.equals(testProductId)) {
				item.updateQuantity(item.getQuantity() + quantity);
				targetItem = item;
				break;
			}
		}
		if (targetItem == null) {
			CartItem newItem = CartItem.builder()
				.cart(cart)
				// .productPostId(productPostId)
				.quantity(quantity)
				.selected(true)
				.build();

			cart.addCartItem(newItem);
			targetItem = newItem;
		}
		cartJpaRepository.save(cart);
		return new CartItemResponse("title", "name", targetItem.getTotalPrice(), targetItem.getQuantity(),
			targetItem.isSelected());

	}

	/**
	 * 장바구니 아이템 수량 변경 <p>
	 * 아이템 ID로 장바구니 아이템 조회 <p>
	 * 수량 업데이트 <p>
	 * 변경사항 저장 <p>
	 */
	public CartItemResponse updateQuantity(String itemId, int quantity) {
		CartItem cartItem = cartItemJpaRepository.findById(itemId)
			.orElseThrow(() -> new CustomException(CartExceptionCode.CARTITEM_NOT_FOUND.getMessage()));

		cartItem.updateQuantity(quantity);

		CartItem savedCartItem = cartItemJpaRepository.save(cartItem);

		return new CartItemResponse("title", "name", savedCartItem.getTotalPrice(), savedCartItem.getQuantity(),
			savedCartItem.isSelected());
	}

	/**
	 * 아이템 ID로 장바구니 아이템 조회 <p>
	 * 선택 상태 업데이트
	 * 변경사항 저장
	 */
	public CartItemResponse setItemSelected(String itemId, boolean selected) {
		CartItem cartItem = cartItemJpaRepository.findById(itemId)
			.orElseThrow(() -> new CustomException(CartExceptionCode.CARTITEM_NOT_FOUND.getMessage()));

		cartItem.setSelected(selected);

		CartItem savedItem = cartItemJpaRepository.save(cartItem);
		return new CartItemResponse("title", "name", savedItem.getTotalPrice(), savedItem.getQuantity(),
			savedItem.isSelected());
	}

	/**
	 * 장바구니에서 아이템 삭제 <p>
	 * 1. 아이템 ID로 장바구니 아이템 조회 <p>
	 * 2. 장바구니와 아이템 간의 연결 해제 <p>
	 * 3. 아이템 삭제 <p>
	 * 4. 장바구니 상태 업데이트
	 */
	public void removeItem(String cartItemId) {
		CartItem cartItem = cartItemJpaRepository.findById(cartItemId)
			.orElseThrow(() -> new CustomException(CartExceptionCode.CARTITEM_NOT_FOUND.getMessage()));

		Cart cart = cartItem.getCart();
		cart.removeCartItem(cartItem);

		cartItemJpaRepository.delete(cartItem);

		cartJpaRepository.save(cart);

	}
}
