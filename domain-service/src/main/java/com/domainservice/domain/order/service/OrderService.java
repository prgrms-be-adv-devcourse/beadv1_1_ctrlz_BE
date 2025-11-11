package com.domainservice.domain.order.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.CartExceptionCode;
import com.common.exception.vo.OrderExceptionCode;
import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.repository.CartItemJpaRepository;
import com.domainservice.domain.order.model.dto.OrderItemResponse;
import com.domainservice.domain.order.model.dto.OrderResponse;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.model.entity.OrderItem;
import com.domainservice.domain.order.model.entity.OrderStatus;
import com.domainservice.domain.order.repository.OrderJpaRepository;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.service.ProductPostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
	private final OrderJpaRepository orderJpaRepository;
	private final CartItemJpaRepository cartItemJpaRepository;
	private final ProductPostService productPostService;

	/**
	 * 주문 생성
	 * 장바구니 아이템 조회
	 * 주문 엔티티 생성
	 * 주문 아이템 생성 및 주문에 추가
	 * 주문 저장
	 */
	public OrderResponse createOrder(String userId, List<String> cartItemIds) {

		List<CartItem> cartItems = cartItemJpaRepository.findAllByIdIn(cartItemIds);

		if (cartItems.isEmpty() || cartItems.size() != cartItemIds.size()) {
			throw new CustomException(CartExceptionCode.CARTITEM_NOT_FOUND.getMessage());
		}

		// 상품 정보 미리 조회 (중복 방지)
		Map<String, ProductPostResponse> productMap = cartItems.stream()
			.collect(Collectors.toMap(
				CartItem::getProductPostId,
				item -> productPostService.getProductPostById(item.getProductPostId())
			));

		String orderName = generateOrderName(cartItems, productMap);

		Order order = Order.builder()
			.buyerId(userId)
			.orderName(orderName)
			.orderStatus(OrderStatus.PAYMENT_PENDING)
			.build();

		for (CartItem cartItem : cartItems) {
			ProductPostResponse product = productMap.get(cartItem.getProductPostId());

			if (!productPostService.isSellingTradeStatus(cartItem.getProductPostId())) {
				throw new CustomException(OrderExceptionCode.PRODUCT_NOT_AVAILABLE.getMessage());
			}

			OrderItem orderItem = OrderItem.builder()
				.quantity(cartItem.getQuantity())
				.priceSnapshot(product.price())
				.build();

			order.addOrderItem(orderItem);
		}

		Order savedOrder = orderJpaRepository.save(order);

		return new OrderResponse(savedOrder.getOrderName(),
			savedOrder.getId(), savedOrder.getBuyerId(), savedOrder.getCreatedAt(),
			savedOrder.getTotalAmount(), savedOrder.getOrderStatus(), savedOrder.getOrderItems()
			.stream()
			.map(x ->
				new OrderItemResponse(x.getId(),
					x.getQuantity(),
					x.getPriceSnapshot(),
					x.getTotalPrice()))
			.toList());
	}

	/**
	 * 주문 취소
	 * 주문 ID와 사용자 ID를 받아 해당 주문을 취소
	 * 주문 상태가 '결제대기' 또는 '결제완료' 상태인 경우에만 취소 가능
	 * 취소된 주문은 결제 대기 시 취소, 결제완료에서는 환불로 상태 변경
	 */
	public OrderResponse cancelOrder(String orderId, String userId) {
		Order order = orderJpaRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderExceptionCode.ORDER_NOT_FOUND.getMessage()));

		if (!order.getBuyerId().equals(userId)) {
			throw new CustomException(OrderExceptionCode.ORDER_UNAUTHORIZED.getMessage());
		}

		if (order.getOrderStatus() != OrderStatus.PAYMENT_PENDING &&
			order.getOrderStatus() != OrderStatus.PAYMENT_COMPLETED) {
			throw new CustomException(OrderExceptionCode.ORDER_CANNOT_CANCEL.getMessage());
		}

		order.setOrderStatus(OrderStatus.CANCEL);

		// TODO
		// 결제 완료 상태였던 경우 추가 처리 (결제 시스템에 환불 요청)
		// paymentService.cancelOrder(order);
		// 정산에도 반영해야됨
		// 정산 전, 정산 후

		// 변경된 주문 저장
		Order savedOrder = orderJpaRepository.save(order);

		// 응답 데이터 구성 및 반환
		return new OrderResponse(
			savedOrder.getOrderName(),
			savedOrder.getId(),
			savedOrder.getBuyerId(),
			savedOrder.getCreatedAt(),
			savedOrder.getTotalAmount(),
			savedOrder.getOrderStatus(),
			savedOrder.getOrderItems()
				.stream()
				.map(x -> new OrderItemResponse(
					x.getId(),
					x.getQuantity(),
					x.getPriceSnapshot(),
					x.getTotalPrice()))
				.toList());
	}

	/**
	 * 주문 확정
	 * 주문자 일치 여부 확인
	 * 주문 상태 확인 및 구매확정 가능 여부 판단
	 * 주문 상태를 구매확정으로 변경
	 * 변경된 주문 저장
	 * 응답 데이터 구성 및 반환
	 */
	public OrderResponse confirmPurchase(String orderId, String userId) {
		Order order = orderJpaRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderExceptionCode.ORDER_NOT_FOUND.getMessage()));

		if (!order.getBuyerId().equals(userId)) {
			throw new CustomException(OrderExceptionCode.ORDER_UNAUTHORIZED.getMessage());
		}

		if (order.getOrderStatus() != OrderStatus.PAYMENT_COMPLETED) {
			throw new CustomException(OrderExceptionCode.ORDER_CANNOT_CONFIRM.getMessage());
		}

		order.setOrderStatus(OrderStatus.PURCHASE_CONFIRMED);

		Order savedOrder = orderJpaRepository.save(order);

		return new OrderResponse(
			savedOrder.getOrderName(),
			savedOrder.getId(),
			savedOrder.getBuyerId(),
			savedOrder.getCreatedAt(),
			savedOrder.getTotalAmount(),
			savedOrder.getOrderStatus(),
			savedOrder.getOrderItems()
				.stream()
				.map(x -> new OrderItemResponse(
					x.getId(),
					x.getQuantity(),
					x.getPriceSnapshot(),
					x.getTotalPrice()))
				.toList());
	}

	/**
	 * 장바구니 아이템 목록으로부터 주문 이름 생성
	 * "첫번째 상품명 외 N건" 형식으로 생성
	 */
	private String generateOrderName(List<CartItem> cartItems, Map<String, ProductPostResponse> productMap) {
		String firstItemName = productMap.get(cartItems.getFirst().getProductPostId()).title();
		return (cartItems.size() == 1)
			? firstItemName
			: firstItemName + " 외 " + (cartItems.size() - 1) + "건";
	}

}
