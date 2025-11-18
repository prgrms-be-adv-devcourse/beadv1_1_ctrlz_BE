package com.domainservice.common.init.data;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.repository.CartItemJpaRepository;
import com.domainservice.domain.order.model.dto.OrderResponse;
import com.domainservice.domain.order.service.OrderService;
import com.domainservice.domain.post.post.service.ProductPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
// @Profile({"local", "dev"})
@RequiredArgsConstructor
public class OrderInitializer {

	private final OrderService orderService;
	private final CartItemJpaRepository cartItemJpaRepository;
	private final ProductPostService productPostService;

	public void init() {
		log.info("--- 주문 초기화 시작 ---");

		List<String> userIds = List.of("user-001", "user-002", "user-003", "user-004", "user-005");

		for (String userId : userIds) {
			// 장바구니 아이템 조회
			List<String> cartItemIdsFromDb = cartItemJpaRepository.findCartItemIdsByUserId(userId);

			// 판매 가능한 상품만 필터 + ProductPostId 기준으로 중복 제거
			List<String> cartItemIds = cartItemIdsFromDb.stream()
				.map(id -> cartItemJpaRepository.findById(id).orElse(null))
				.filter(item -> item != null && productPostService.isSellingTradeStatus(item.getProductPostId()))
				.collect(Collectors.groupingBy(CartItem::getProductPostId))
				.values().stream()
				.map(list -> list.get(0)) // 같은 상품은 하나만
				.map(CartItem::getId)
				.toList();

			if (cartItemIds.isEmpty()) {
				log.warn("{}님의 주문 생성 실패: 판매 가능한 상품이 없습니다.", userId);
				continue;
			}

			try {
				OrderResponse order = orderService.createOrder(userId, cartItemIds);
				log.info("order.orderId() = {}", order.orderId());
				log.info("{}님의 주문 생성 완료", userId);
			} catch (Exception e) {
				log.warn("주문 생성 실패 ({}): {}", userId, e.getMessage());
			}
		}

		log.info("--- 주문 초기화 완료 ---");
	}
}
