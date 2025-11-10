package com.domainservice.domain.order.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.domainservice.domain.order.model.dto.CreateOrderRequest;
import com.domainservice.domain.order.model.dto.OrderResponse;
import com.domainservice.domain.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	// 주문 생성
	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(
		// @RequestHeader("X-USER-ID") String userId,
		@RequestBody CreateOrderRequest orderRequest) {

		// TODO 유저아이디
		String userId = "TESTUSER";
		return ResponseEntity.ok(
			orderService.createOrder(userId, orderRequest.cartItemIds())
		);
	}

	// 주문 취소
	@PatchMapping("/{orderId}/cancel")
	public ResponseEntity<OrderResponse> cancelOrder(
		// @RequestHeader("X-USER-ID") String userId,
		@PathVariable String orderId) {
		String userId = "TESTUSER";
		return ResponseEntity.ok(
			orderService.cancelOrder(orderId, userId)
		);
	}

	// 주문 확정
	@PatchMapping("/{orderId}/confirm")
	public ResponseEntity<OrderResponse> confirmPurchase(
		// @RequestHeader("X-USER-ID") String userId,
		@PathVariable String orderId) {
		String userId = "TESTUSER";
		return ResponseEntity.ok(
			orderService.confirmPurchase(orderId, userId)
		);
	}

	// TODO: 주문 목록 조회
	// TODO: 주문 상세 조회
}