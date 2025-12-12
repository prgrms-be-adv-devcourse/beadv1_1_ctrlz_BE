package com.domainservice.domain.order.api;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.common.model.web.PageResponse;
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
	public BaseResponse<OrderResponse> createOrder(
			@RequestHeader(value = "X-REQUEST-ID") String userId,
			@RequestBody CreateOrderRequest orderRequest) {

		// TODO 유저아이디
		return new BaseResponse<>(
				orderService.createOrder(userId, orderRequest.cartItemIds()),
				"주문 생성 성공했습니다");
	}

	// 주문 취소
	@PatchMapping("/{orderId}/cancel")
	public BaseResponse<OrderResponse> cancelOrder(
			@RequestHeader(value = "X-REQUEST-ID") String userId,
			@PathVariable String orderId) {
		return new BaseResponse<>(
				orderService.cancelOrder(orderId, userId), "주문 취소 성공했습니다");
	}

	// 주문 확정
	@PatchMapping("/{orderId}/confirm")
	public BaseResponse<OrderResponse> confirmPurchase(
			@RequestHeader(value = "X-REQUEST-ID") String userId,
			@PathVariable String orderId) {
		return new BaseResponse<>(orderService.confirmPurchase(orderId, userId),
				"주문 확정 성공했습니다");
	}

	// 주문 일부 취소
	@PatchMapping("/{orderId}/items/{orderItemId}/cancel")
	public BaseResponse<OrderResponse> cancelOrderItem(
			@RequestHeader(value = "X-REQUEST-ID") String userId,
			@PathVariable String orderId,
			@PathVariable String orderItemId) {
		return new BaseResponse<>(
				orderService.cancelOrderItem(orderId, userId, orderItemId),
				"주문 일부 취소 성공했습니다");
	}

	// 주문 상세 조회
	@GetMapping("/{orderId}")
	public BaseResponse<OrderResponse> getOrder(
			@PathVariable String orderId,
			@RequestHeader(value = "X-REQUEST-ID") String userId) {
		OrderResponse orderById = orderService.getOrderById(orderId, userId);
		return new BaseResponse<>(
				orderById,
				"주문 상세 조회 성공했습니다");

	}

	// 주문 목록 조회
	@GetMapping
	public PageResponse<List<OrderResponse>> getOrderList(
			@RequestHeader(value = "X-REQUEST-ID") String userId,
			@PageableDefault(size = 10) Pageable pageable) {
		PageResponse<List<OrderResponse>> orderListByUserId = orderService.getOrderListByUserId(userId, pageable);

		return orderListByUserId;
	}

	// 정산용 주문 목록 조회 (배치에서 호출)
	@GetMapping("/settlement")
	public BaseResponse<List<OrderResponse>> getOrdersForSettlement(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
		return new BaseResponse<>(
				orderService.getOrdersForSettlement(startDate, endDate),
				"정산용 주문 목록 조회 성공했습니다");
	}
}