
package com.aiservice.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.RecommendationResult;
import com.aiservice.domain.model.UserContext;
import com.aiservice.domain.vo.RecommandationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductRecommendationService implements RecommendService {

	@Value("${custom.recommendation.limit:10}")
	private int recommendationLimit;

	@Value("${custom.product.base-url:http://localhost:8080/product}")
	private String productBaseUrl;

	private final SessionService sessionService;
	private final ChatClient chatClient;
	private final UserContextService userContextService;
	private final HybridSearchProcessor hybridSearchProcessor;

	@Override
	public void recommendProductsByQuery(String userId, String query) {
		log.info("추천 생성 시작 - 사용자: {}, 쿼리: {}", userId, query);

		long currentCount = sessionService.getRecommendationCount(userId);
		if (currentCount >= recommendationLimit) {
			log.info("사용자 추천 제한 ({}) 도달: {}", recommendationLimit, userId);
			sessionService.publishRecommandationData(userId, RecommendationResult.limitReached());
			return;
		}

		List<DocumentSearchResponse> searchResults = hybridSearchProcessor.search(query, 20);

		// Generate personalized message
		RecommendationResult result = Optional.ofNullable(
			generateRecommendationMessage(userId, query, searchResults)
		).map(msg -> RecommendationResult.builder()
			.status(RecommandationStatus.OK)
			.message(msg)
			.items(searchResults)
			.build()
		).orElseGet(RecommendationResult::noResults);

		// Store in session
		sessionService.publishRecommandationData(userId, result);
		sessionService.incrementRecommendationCount(userId);
		log.info("{} 개의 추천 결과 저장 완료 - 사용자: {} (쿼리: {})", searchResults.size(), userId, query);
	}

	private String generateRecommendationMessage(String userId, String target,
		List<DocumentSearchResponse> recommendations) {
		if (recommendations.isEmpty()) {
			return null;
		}

		// 1. User Context
		String userContext = "";
		UserContext ctx = userContextService.getUserContext(userId);
		if (ctx != null) {
			userContext = String.format(
				"사용자 정보: [성별: %s, 나이: %d세, 최근 검색어: %s, 장바구니 상품: %s]",
				ctx.gender(), ctx.age(),
				String.join(", ", ctx.recentSearchKeywords()),
				String.join(", ", ctx.cartProductNames()));
		}

		// 2. Search Context
		String searchContext = String.format("사용자가 '%s'라고 검색했습니다.", target);

		// 3. Product List
		StringBuilder productList = new StringBuilder();
		for (DocumentSearchResponse doc : recommendations) {
			String productId = (String)doc.metadata().get("productId");
			Integer price = (Integer)doc.metadata().get("price");
			String url = productBaseUrl + "/" + productId;
			productList.append(String.format("- 가격: %d원, URL: %s\n", price, url));
		}

		// 4. Load prompt template
		PromptTemplate promptTemplate = new PromptTemplate(
			new ClassPathResource("prompts/recommendation.st"));

		String finalPrompt = promptTemplate.render(Map.of(
			"searchContext", searchContext,
			"userContext", userContext,
			"productList", productList.toString()));

		try {
			return chatClient.prompt()
				.user(finalPrompt)
				.call()
				.content();
		} catch (Exception e) {
			log.error("추천 메시지 생성 실패", e);
			return null;
		}
	}
}
