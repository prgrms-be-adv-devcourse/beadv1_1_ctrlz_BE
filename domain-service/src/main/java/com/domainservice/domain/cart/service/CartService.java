package com.domainservice.domain.cart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
		List<CartItemResponse> response = new ArrayList<>();
		List<CartItem> cartItemList = cartItemJpaRepository.findByCart(cart);

		for (CartItem cartItem : cartItemList) {
			ProductPostResponse productPostById = productPostService.getProductPostById(cartItem.getProductPostId());
			CartItemResponse cartItemResponse = new CartItemResponse(productPostById.title(), productPostById.name(),
				BigDecimal.valueOf(productPostById.price()),
				cartItem.isSelected());
			response.add(cartItemResponse);
		}

		return response;
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

				// 재고가 있을 때의 경우임
				// item.updateQuantity(item.getQuantity() + quantity);
				targetItem = item;
				throw new CustomException(CartExceptionCode.CARTITEM_ALREADY_ADDED.getMessage());
			}
		}
		if (targetItem == null) {
			CartItem newItem = CartItem.builder()
				.cart(cart)
				.productPostId(productPostId)
				.selected(true)
				.build();

			cart.addCartItem(newItem);
			targetItem = newItem;
		}
		cartJpaRepository.save(cart);

		ProductPostResponse productPostById = productPostService.getProductPostById(targetItem.getProductPostId());
		return new CartItemResponse(productPostById.title(), productPostById.name(),BigDecimal.valueOf(productPostById.price()),
			targetItem.isSelected());

	}

	// /**
	//  * 장바구니 아이템 수량 변경 <p>
	//  * 아이템 ID로 장바구니 아이템 조회 <p>
	//  * 수량 업데이트 <p>
	//  * 변경사항 저장 <p>
	//  */
	// public CartItemResponse updateQuantity(String itemId, int quantity) {
	// 	CartItem cartItem = cartItemJpaRepository.findById(itemId)
	// 		.orElseThrow(() -> new CustomException(CartExceptionCode.CARTITEM_NOT_FOUND.getMessage()));
	//
	// 	cartItem.updateQuantity(quantity);
	//
	// 	CartItem savedCartItem = cartItemJpaRepository.save(cartItem);
	// 	ProductPostResponse productPostById = productPostService.getProductPostById(savedCartItem.getProductPostId());
	// 	return new CartItemResponse(productPostById.title(), productPostById.name(), getTotalPrice(savedCartItem),
	// 		savedCartItem.getQuantity(),
	// 		savedCartItem.isSelected());
	// }

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

		ProductPostResponse productPostById = productPostService.getProductPostById(savedItem.getProductPostId());
		return new CartItemResponse(productPostById.title(),
			productPostById.name(),
			BigDecimal.valueOf(productPostById.price()),
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
