package com.domainservice.domain.cart.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.cart.model.dto.CreateCartRequest;
import com.domainservice.domain.cart.model.dto.response.CartItemResponse;
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
		// TODO 유저 정보는 이후 수정
		String userId = "test";
		return new BaseResponse<>(cartService.getCartItemList(userId), "장바구니 아이템 리스트 조회 성공했습니다");
	}

	/**
	 *  장바구니에 상품 추가
	 * - 기존 아이템이 있으면 수량만 증가
	 */
	@PostMapping("/items")
	public BaseResponse<CartItemResponse> addItemToCart(
		@RequestParam("productPostId") String productPostId, @RequestParam("quantity") int quantity) {
		// TODO 유저 정보는 이후 수정
		String userId = "testUser";
		return new BaseResponse<>(cartService.addItem(userId, productPostId, quantity)
			, "장바구니 아이템 수량 변경 성공했습니다");
	}

	/**
	 *  장바구니 아이템 수량 변경
	 */
	@PatchMapping("/items/{itemId}/quantity")
	public BaseResponse<CartItemResponse> updateItemQuantity(@PathVariable String itemId, @RequestParam int quantity) {
		return new BaseResponse<>(
			cartService.updateQuantity(itemId, quantity), "장바구니 아이템 수량 변경 성공했습니다");
	}

	/**
	 *  장바구니 아이템 선택/해제
	 */
	@PatchMapping("/items/{itemId}/select")
	public BaseResponse<CartItemResponse> toggleItemSelection(@PathVariable String itemId,
		@RequestParam boolean selected) {
		return new BaseResponse<>(
			cartService.setItemSelected(itemId, selected), "장바구니 아이템 수량 변경 성공했습니다");
	}

	/**
	 * 5 장바구니 아이템 삭제
	 */
	@DeleteMapping("/items/{itemId}")
	public BaseResponse<Void> removeCartItem(@PathVariable String itemId) {
		cartService.removeItem(itemId);
		return new BaseResponse<>(null, "삭제 완료");
	}

	/**
	 * 장바구니 생성
	 * 사용자별 장바구니 생성 (이미 존재하는 경우 에러 반환)
	 */
	@PostMapping
	public ResponseEntity<Void> createCart(@RequestBody CreateCartRequest request) {
		cartService.addCart(request.userId());
		return ResponseEntity.ok().build();
	}
}
