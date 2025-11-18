package com.domainservice.common.init.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.domainservice.domain.cart.model.entity.Cart;
import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.repository.CartJpaRepository;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.domainservice.domain.post.post.service.ProductPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
// @Profile({"local", "dev"})
@RequiredArgsConstructor
public class CartInitializer {

	private final CartJpaRepository cartRepository;
	private final ProductPostRepository productPostRepository;
	private final ProductPostService productPostService;
	private final Random random = new Random();

	public void init() {
		log.info("--- 장바구니 초기화 시작 ---");

		List<String> userIds = List.of("user-001", "user-002", "user-003", "user-004", "user-005");

		int totalCarts = 0;

		for (String userId : userIds) {
			// 자기 상품 제외하고 상품 목록 조회
			List<String> availableProductPostIds = productPostRepository.findAllIdsExceptOwner(userId)
				.stream()
				.filter(productPostService::isSellingTradeStatus)
				.toList();
			if (availableProductPostIds.isEmpty()) {
				log.warn("userId={} 사용자의 장바구니에 넣을 수 있는 상품이 없습니다.", userId);
				continue;
			}

			Cart cart = Cart.builder()
				.userId(userId)
				.cartItems(new ArrayList<>())
				.build();

			int itemCount = 2 + random.nextInt(4); // 2~5개 랜덤 아이템
			for (int i = 0; i < itemCount; i++) {
				String productId = availableProductPostIds.get(random.nextInt(availableProductPostIds.size()));

				CartItem cartItem = CartItem.builder()
					.productPostId(productId)
					.selected(true)
					.build();

				cart.addCartItem(cartItem);
			}

			cartRepository.save(cart);
			totalCarts++;
		}

		log.info("총 {}개의 장바구니 초기화 완료 (자기 상품 제외 처리됨)", totalCarts);
	}
}