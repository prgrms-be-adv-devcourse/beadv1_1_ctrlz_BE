package com.domainservice.domain.cart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.CartExceptionCode;
import com.domainservice.domain.cart.model.dto.response.CartItemResponse;
import com.domainservice.domain.cart.model.entity.Cart;
import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.repository.CartItemJpaRepository;
import com.domainservice.domain.cart.repository.CartJpaRepository;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.service.ProductPostService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
	private final CartJpaRepository cartJpaRepository;
	private final CartItemJpaRepository cartItemJpaRepository;
	private final ProductPostService productPostService;

	/**
	 * 1. userid로 장바구니 조회 - 없으면 생성 <p></p>
	 * 2. 장바구니id로 장바구니 아이템 리스트 조회
	 */
	@Transactional(readOnly = true)
	public List<CartItemResponse> getCartItemList(String userId) {

		Cart cart = getCartByUserId(userId);

		return cartItemJpaRepository.findByCart(cart).stream()
			.map(x -> new CartItemResponse("title", "name", getTotalPrice(x), x.getQuantity(), x.isSelected()))
			.toList();
	}

	public Cart getCartByUserId(String userId) {
		return cartJpaRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(CartExceptionCode.CART_NOT_FOUND.getMessage()));
	}

	public void addCart(String userId) {
		Optional<Cart> cart = cartJpaRepository.findByUserId(userId);
		if (cart.isPresent()) {
			throw new CustomException(CartExceptionCode.CART_ALREADY_EXISTS.getMessage());
		}
		cartJpaRepository.save(Cart.builder().userId(userId).build());
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

		for (CartItem item : cartItems) {
			if (productPostId.equals(item.getProductPostId())) {
				item.updateQuantity(item.getQuantity() + quantity);
				targetItem = item;
				break;
			}
		}
		if (targetItem == null) {
			CartItem newItem = CartItem.builder()
				.cart(cart)
				.productPostId(productPostId)
				.quantity(quantity)
				.selected(true)
				.build();

			cart.addCartItem(newItem);
			targetItem = newItem;
		}
		cartJpaRepository.save(cart);
		return new CartItemResponse("title", "name", getTotalPrice(targetItem), targetItem.getQuantity(),
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

		return new CartItemResponse("title", "name", getTotalPrice(savedCartItem), savedCartItem.getQuantity(),
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
		return new CartItemResponse("title", "name", getTotalPrice(savedItem), savedItem.getQuantity(),
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

	private int getTotalPrice(CartItem cartItem) {
		int totalPrice = 0;
		ProductPostResponse productPostById = productPostService.getProductPostById(cartItem.getProductPostId());
		totalPrice += productPostById.price() * cartItem.getQuantity();

		return totalPrice;
	}
}
