package com.domainservice.domain.cart.api;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.cart.model.dto.response.CartItemResponse;
import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.service.CartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
	private final CartService cartService;

	/**
	 *  장바구니 조회
	 *  현재 로그인한 사용자의 장바구니 내역 전체 조회
	 */
	@GetMapping
	public BaseResponse<List<CartItemResponse>> getMyCart() {
		// TODO 유저 정보는
		String userId = "test";
		// TODO
		List<CartItem> cartItemList = cartService.getCartItemList(userId);
		return new BaseResponse<>(cartItemList.stream()
			.map(x -> new CartItemResponse("title", "name", x.getTotalPrice(), x.getQuantity(), x.isSelected()))
			.toList(), "장바구니 아이템 리스트 조회 성공했습니다");
	}

	/**
	 *  장바구니에 상품 추가
	 * - 기존 아이템이 있으면 수량만 증가
	 */
	@PostMapping("/items")
	public BaseResponse<CartItemResponse> addItemToCart(
		@RequestParam("productPostId") String productPostId, @RequestParam("quantity") int quantity) {
		String userId = "testUser";
		CartItem addedItem = cartService.addItem(userId, productPostId, quantity);
		return new BaseResponse<>(
			new CartItemResponse("title", "name", addedItem.getTotalPrice(), addedItem.getQuantity(),
				addedItem.isSelected()), "장바구니 아이템 수량 변경 성공했습니다");
	}

	/**
	 *  장바구니 아이템 수량 변경
	 */
	@PatchMapping("/items/{itemId}/quantity")
	public BaseResponse<CartItemResponse> updateItemQuantity(@PathVariable String itemId, @RequestParam int quantity) {
		CartItem updatedItem = cartService.updateQuantity(itemId, quantity);
		return new BaseResponse<>(
			new CartItemResponse("title", "name", updatedItem.getTotalPrice(), updatedItem.getQuantity(),
				updatedItem.isSelected()), "장바구니 아이템 수량 변경 성공했습니다");
	}

	/**
	 *  장바구니 아이템 선택/해제
	 */
	@PatchMapping("/items/{itemId}/select")
	public BaseResponse<CartItemResponse> toggleItemSelection(@PathVariable String itemId,
		@RequestParam boolean selected) {
		CartItem updatedItem = cartService.setItemSelected(itemId, selected);
		return new BaseResponse<>(
			new CartItemResponse("title", "name", updatedItem.getTotalPrice(), updatedItem.getQuantity(),
				updatedItem.isSelected()), "장바구니 아이템 수량 변경 성공했습니다");
	}

	/**
	 * 5 장바구니 아이템 삭제
	 */
	@DeleteMapping("/items/{itemId}")
	public BaseResponse<Void> removeCartItem(@PathVariable String itemId) {
		cartService.removeItem(itemId);
		return new BaseResponse<>(null, "삭제 완료");
	}

}
