package com.aiservice.application;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.UserContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM을 사용하여 추천 메시지를 생성하는 서비스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationMessageGenerator {

	@Value("${custom.product.base-url:http://localhost:8080/product}")
	private String productBaseUrl;

	private final ChatClient chatClient;
	private final UserContextService userContextService;
	private final PromptTemplate promptTemplate;
	private final ChatOptions chatOptions;

	public String toPrompt(String userId, String query, List<DocumentSearchResponse> recommendations) {

		if (recommendations.isEmpty()) {
			return null;
		}

		String userContext = buildUserContext(userId);
		String searchContext = "사용자가 '%s'라고 검색했습니다.".formatted(query);
		String productList = buildProductList(recommendations);

		String finalPrompt = promptTemplate.render(Map.of(
			"searchContext", searchContext,
			"userContext", userContext,
			"productList", productList));

		try {
			return chatClient.prompt()
				.system(finalPrompt)
				.user(query)
				.options(chatOptions)
				.call()
				.content();
		} catch (Exception e) {
			log.error("추천 메시지 생성 실패", e);
			return null;
		}
	}

	private String buildUserContext(String userId) {
		UserContext context = userContextService.getUserContext(userId);
		
		return String.format(
			"""
			사용자 정보: [성별: %s, 나이: %d세, 최근 검색어: %s, 최근 30일 간 장바구니 상품들: %s]
			최근 조회한 상품들 : [%s]
			""",
			 context.gender(),  context.age(),
			String.join(", ",  context.searchKeywords()),
			String.join(", ",  context.cartProductNames()),
			String.join(", ",  context.viewedTitle())
		);
	}

	private String buildProductList(List<DocumentSearchResponse> recommendations) {
		StringBuilder productList = new StringBuilder();
		for (DocumentSearchResponse doc : recommendations) {
			String productId = (String)doc.metadata().get("productId");
			Integer price = (Integer)doc.metadata().get("price");
			String url = productBaseUrl + "/" + productId;
			productList.append("- 가격: %d원, URL: %s\n".formatted(price, url));
		}
		return productList.toString();
	}
}
