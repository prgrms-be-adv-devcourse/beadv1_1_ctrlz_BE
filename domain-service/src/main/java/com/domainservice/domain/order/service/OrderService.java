package com.domainservice.domain.order.service;

import java.math.BigDecimal;
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
import com.domainservice.domain.order.model.entity.OrderItemStatus;
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
	 * Order -> PAYMENT_PENDING
	 * OrderItem -> PAYMENT_PENDING
	 */
	public OrderResponse createOrder(String userId, List<String> cartItemIds) {

		List<CartItem> cartItems = cartItemJpaRepository.findAllByIdIn(cartItemIds);

		if (cartItems.isEmpty() || cartItems.size() != cartItemIds.size()) {
			throw new CustomException(CartExceptionCode.CARTITEM_NOT_FOUND.getMessage());
		}

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
			productPostService.updateTradeStatusToProcessing(cartItem.getProductPostId());

			OrderItem orderItem = OrderItem.builder()
				.productPostId(product.id())
				.priceSnapshot(BigDecimal.valueOf(product.price()))
				.orderItemStatus(OrderItemStatus.PAYMENT_PENDING)
				.build();

			order.addOrderItem(orderItem);
		}

		Order savedOrder = orderJpaRepository.save(order);

		return toOrderResponse(savedOrder);

	}

	/**
	 * 주문 취소 요청
	 * - 결제 상태에 따라 결제전/결제후 취소 메서드 분기
	 * Order -> 결제전 : CANCELLED, 결제후 : REFUND_AFTER_PAYMENT
	 * OrderItem -> 결제전 : CANCELLED, 결제후 : REFUND_AFTER_PAYMENT
	 */
	public OrderResponse cancelOrder(String orderId, String userId) {
		Order order = orderJpaRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderExceptionCode.ORDER_NOT_FOUND.getMessage()));

		if (!order.getBuyerId().equals(userId)) {
			throw new CustomException(OrderExceptionCode.ORDER_UNAUTHORIZED.getMessage());
		}
		switch (order.getOrderStatus()) {
			case PAYMENT_PENDING -> {
				order.orderCanceled();
			}
			case PAYMENT_COMPLETED -> {
				// TODO PG사 환불 처리
				// paymentService.refund(order);
				order.orderRefundedAfterPayment();
			}
			default -> throw new CustomException(OrderExceptionCode.ORDER_CANNOT_CANCEL.getMessage());
		}

		for (OrderItem item : order.getOrderItems()) {
			productPostService.updateTradeStatusToSelling(item.getProductPostId());
		}
		return toOrderResponse(orderJpaRepository.save(order));
	}

	/**
	 * 주문 일부 취소
	 * 주문 ID, 사용자 ID와 주문 아이템 ID 를 받아 해당 주문아이템만 취소
	 * 주문 상태가 '결제대기' 또는 '결제완료' 상태인 경우에만 취소 가능
	 * 취소된 주문은 결제 대기 시 취소, 결제완료에서는 환불로 상태 변경
	 */
	public OrderResponse cancelOrderItem(String orderId, String userId, String orderItemId) {
		Order order = orderJpaRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderExceptionCode.ORDER_NOT_FOUND.getMessage()));

		if (!order.getBuyerId().equals(userId)) {
			throw new CustomException(OrderExceptionCode.ORDER_UNAUTHORIZED.getMessage());
		}

		OrderItem targetItem = order.getOrderItems().stream()
			.filter(item -> item.getId().equals(orderItemId))
			.findFirst()
			.orElseThrow(() -> new CustomException(OrderExceptionCode.ORDERITEM_NOT_FOUND.getMessage()));

		switch (order.getOrderStatus()) {
			case PAYMENT_PENDING:
				targetItem.setOrderItemStatus(OrderItemStatus.CANCELLED);
				productPostService.updateTradeStatusToSelling(targetItem.getProductPostId());
				break;
			case PAYMENT_COMPLETED:
				// paymentService.refundItem(targetItem);
				targetItem.setOrderItemStatus(OrderItemStatus.REFUND_AFTER_PAYMENT);
				productPostService.updateTradeStatusToSelling(targetItem.getProductPostId());
				break;
			default:
				throw new CustomException(OrderExceptionCode.ORDER_CANNOT_CANCEL.getMessage());
		}

		boolean allCanceledOrRefunded = order.getOrderItems().stream()
			.allMatch(item -> item.getOrderItemStatus() == OrderItemStatus.CANCELLED
				|| item.getOrderItemStatus() == OrderItemStatus.REFUND_AFTER_PAYMENT);

		// 모든 아이템이 취소/환불이면 주문 상태도 변경
		if (allCanceledOrRefunded) {
			if (order.getOrderStatus() == OrderStatus.PAYMENT_PENDING) {
				order.setOrderStatus(OrderStatus.CANCELLED);
			} else if (order.getOrderStatus() == OrderStatus.PAYMENT_COMPLETED) {
				order.setOrderStatus(OrderStatus.REFUND_AFTER_PAYMENT);
			}
		}

		Order savedOrder = orderJpaRepository.save(order);
		return toOrderResponse(savedOrder);

	}

	/**
	 * 주문 구매 확정
	 * 주문자 일치 여부 확인
	 * 주문 상태 확인 및 구매확정 가능 여부 판단
	 * 주문 상태를 구매확정으로 변경
	 * 변경된 주문 저장
	 * 응답 데이터 구성 및 반환
	 * ORDER -> PURCHASE_CONFIRMED
	 * ORDERItem -> PURCHASE_CONFIRMED
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
		
		// 주문 상태만 변경
		order.setOrderStatus(OrderStatus.PURCHASE_CONFIRMED);
		// 각 아이템 상태는 부분취소 여부에 따라 조건부 변경
		for (OrderItem item : order.getOrderItems()) {
			if (item.getOrderItemStatus() == OrderItemStatus.PAYMENT_COMPLETED) {
				item.setOrderItemStatus(OrderItemStatus.PURCHASE_CONFIRMED);
				productPostService.updateTradeStatusToSoldout(item.getProductPostId());
			}
		}

		Order savedOrder = orderJpaRepository.save(order);

		//todo 정산 서비스에 저장?
		return toOrderResponse(savedOrder);
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

	/**
	 * 공통 응답 변환
	 */
	private OrderResponse toOrderResponse(Order order) {
		return new OrderResponse(
			order.getOrderName(),
			order.getId(),
			order.getBuyerId(),
			order.getCreatedAt(),
			order.getTotalAmount(),
			order.getOrderStatus(),
			order.getOrderItems().stream()
				.map(x -> new OrderItemResponse(
					x.getId(),
					x.getPriceSnapshot(),
					x.getOrderItemStatus()))
				.toList()
		);
	}
}
