package com.domainservice.domain.order.api;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.order.model.dto.CreateOrderRequest;
import com.domainservice.domain.order.model.dto.OrderResponse;
import com.domainservice.domain.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
	String USERID = "user-001";

	private final OrderService orderService;

	// 주문 생성
	@PostMapping
	public BaseResponse<OrderResponse> createOrder(
		// @RequestHeader("X-USER-ID") String userId,
		@RequestBody CreateOrderRequest orderRequest) {

		// TODO 유저아이디
		return new BaseResponse<>(
			orderService.createOrder(USERID, orderRequest.cartItemIds()),
			"주문 생성 성공했습니다");
	}

	// 주문 취소
	@PatchMapping("/{orderId}/cancel")
	public BaseResponse<OrderResponse> cancelOrder(
		// @RequestHeader("X-USER-ID") String userId,
		@PathVariable String orderId) {
		return new BaseResponse<>(
			orderService.cancelOrder(orderId, USERID), "주문 취소 성공했습니다");
	}

	// 주문 확정
	@PatchMapping("/{orderId}/confirm")
	public BaseResponse<OrderResponse> confirmPurchase(
		// @RequestHeader("X-USER-ID") String userId,
		@PathVariable String orderId) {
		return new BaseResponse(orderService.confirmPurchase(orderId, USERID),
			"주문 확정 성공했습니다");
	}

	// 주문 일부 취소
	@PatchMapping("/{orderId}/items/{orderItemId}/cancel")
	public BaseResponse<OrderResponse> cancelOrderItem(
		// @RequestHeader("X-USER-ID") String userId,
		@PathVariable String orderId,
		@PathVariable String orderItemId
	) {
		return new BaseResponse<>(
			orderService.cancelOrderItem(orderId, USERID, orderItemId),
			"주문 일부 취소 성공했습니다"
		);
	}
	// TODO: 주문 목록 조회
	// TODO: 주문 상세 조회
}