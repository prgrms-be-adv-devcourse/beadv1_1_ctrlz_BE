package com.aiservice.application;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.aiservice.domain.model.UserBehavior;
import com.aiservice.domain.model.UserBehaviorType;
import com.aiservice.domain.model.UserContext;
import com.aiservice.domain.repository.UserBehaviorRepository;
import com.aiservice.infrastructure.feign.DomainServiceClient;
import com.aiservice.infrastructure.feign.UserInfoClient;
import com.aiservice.infrastructure.feign.dto.CartItemResponse;
import com.aiservice.infrastructure.feign.dto.UserDemographicDescription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserContextProvider implements UserContextService {

	private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

	private final UserBehaviorRepository userBehaviorRepository;
	private final UserInfoClient userInfoClient;
	private final DomainServiceClient domainServiceClient;

	@Override
	public UserContext getUserContext(String userId) {
		try {
			return getUserContextAsync(userId).join();
		} catch (Exception e) {
			log.warn("사용자 컨텍스트 조회 실패 - userId: {}, 에러: {}", userId, e.getMessage());
			return UserContext.builder().build();
		}
	}


	public CompletableFuture<UserContext> getUserContextAsync(String userId) {
		// 1. DB에서 사용자 행동 이력 조회 (비동기)
		CompletableFuture<List<UserBehavior>> behaviorsFuture = CompletableFuture.supplyAsync(
				() -> userBehaviorRepository.findByUserId(userId),
				VIRTUAL_EXECUTOR)
				.orTimeout(2, TimeUnit.SECONDS)
				.exceptionally(ex -> {
					log.warn("사용자 행동 이력 조회 실패 - userId: {}, 에러: {}", userId, ex.getMessage());
					return List.of(); 
				});

		// 2. 사용자 정보 조회 (비동기)
		CompletableFuture<UserDemographicDescription> demographicFuture = CompletableFuture.supplyAsync(
				() -> userInfoClient.getRecommendationInfo(userId),
				VIRTUAL_EXECUTOR)
				.orTimeout(2, TimeUnit.SECONDS)
				.exceptionally(ex -> {
					log.warn("유저정보 조회 실패 - userId: {}, 에러: {}", userId, ex.getMessage());
					return new UserDemographicDescription(0, null); 
				});

		// 3. 장바구니 정보 조회 (비동기)
		CompletableFuture<List<CartItemResponse>> cartFuture = CompletableFuture.supplyAsync(
				() -> domainServiceClient.getRecentCartItems(userId),
				VIRTUAL_EXECUTOR)
				.orTimeout(2, TimeUnit.SECONDS)
				.exceptionally(ex -> {
					log.warn("장바구니 정보 조회 실패 - userId: {}, 에러: {}", userId, ex.getMessage());
					return List.of(); 
				});

		// 모든 비동기 작업 완료 대기 후 결과 조합
		return CompletableFuture.allOf(behaviorsFuture, demographicFuture, cartFuture)
				.thenApply(v -> {
					List<UserBehavior> userBehaviors = behaviorsFuture.join();
					UserDemographicDescription recommendationInfo = demographicFuture.join();
					List<CartItemResponse> cartItems = cartFuture.join();

					// 검색어 추출
					List<String> searchTerms = userBehaviors.stream()
							.filter(i -> i.getType() == UserBehaviorType.SEARCH)
							.map(UserBehavior::getValue)
							.toList();

					// 조회 이력 추출
					List<String> viewedTitles = userBehaviors.stream()
							.filter(i -> i.getType() == UserBehaviorType.VIEW)
							.map(UserBehavior::getValue)
							.toList();

					// 장바구니 상품명 추출
					List<String> cartProductNames = cartItems.stream()
							.map(CartItemResponse::name)
							.toList();

					log.debug("유저 컨텍스트 조회 완료 - userId: {}, 검색어: {}, 조회 이력: {}, 장바구니: {}",
							userId, searchTerms.size(), viewedTitles.size(), cartProductNames.size());

					return UserContext.builder()
							.gender(recommendationInfo.gender())
							.age(recommendationInfo.age())
							.searchKeywords(searchTerms)
							.cartProductNames(cartProductNames)
							.viewedTitle(viewedTitles)
							.build();
				});
	}
}
