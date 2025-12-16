package com.domainservice.domain.cart.api;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.cart.docs.AddItemToCartApiDocs;
import com.domainservice.domain.cart.docs.CreateCartApiDocs;
import com.domainservice.domain.cart.docs.GetMyCartApiDocs;
import com.domainservice.domain.cart.docs.GetRecentCartItemsApiDocs;
import com.domainservice.domain.cart.docs.RemoveCartItemApiDocs;
import com.domainservice.domain.cart.docs.ToggleItemSelectionApiDocs;
import com.domainservice.domain.cart.model.dto.CreateCartRequest;
import com.domainservice.domain.cart.model.dto.response.CartItemResponse;
import com.domainservice.domain.cart.service.CartService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
	private final CartService cartService;

	@GetMyCartApiDocs
	@GetMapping
	public BaseResponse<List<CartItemResponse>> getMyCart(
			@RequestHeader(value = "X-REQUEST-ID") String userId) {
		return new BaseResponse<>(cartService.getCartItemList(userId), "장바구니 아이템 리스트 조회 성공했습니다");
	}

	@AddItemToCartApiDocs
	@PostMapping("/items")
	public BaseResponse<CartItemResponse> addItemToCart(
			@RequestParam("productPostId") String productPostId,
			@RequestHeader(value = "X-REQUEST-ID") String userId) {
		return new BaseResponse<>(cartService.addItem(userId, productPostId, 1), "장바구니 아이템 추가 성공했습니다");
	}

	@ToggleItemSelectionApiDocs
	@PatchMapping("/items/{itemId}/select")
	public BaseResponse<CartItemResponse> toggleItemSelection(@PathVariable String itemId,
			@RequestParam boolean selected) {
		return new BaseResponse<>(
				cartService.setItemSelected(itemId, selected), selected ? "장바구니에서 체크선택 했습니다" : "장바구니에서 체크해제했습니다");
	}

	@RemoveCartItemApiDocs
	@DeleteMapping("/items/{itemId}")
	public BaseResponse<Void> removeCartItem(@PathVariable String itemId) {
		cartService.removeItem(itemId);
		return new BaseResponse<>(null, "삭제 완료");
	}

	@CreateCartApiDocs
	@PostMapping
	public void createCart(@RequestBody CreateCartRequest request) {
		cartService.addCart(request.userId());
	}

	@GetRecentCartItemsApiDocs
	@GetMapping("/recent/{userId}")
	public List<CartItemResponse> getRecentCartItems(
			@PathVariable("userId") String userId
	) {
		return cartService.getRecentCartItems(userId);

	}
}
