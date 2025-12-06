package com.aiservice.application;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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

	/**
	 * 사용자 검색 쿼리와 추천 상품 목록을 기반으로 LLM 메시지를 생성
	 *
	 * @param userId          사용자 ID
	 * @param query           검색 쿼리
	 * @param recommendations 추천 상품 목록
	 * @return 생성된 추천 메시지, 실패 시 null
	 */
	public String toPrompt(String userId, String query, List<DocumentSearchResponse> recommendations) {
		if (recommendations.isEmpty()) {
			return null;
		}

		String userContext = buildUserContext(userId);
		String searchContext = String.format("사용자가 '%s'라고 검색했습니다.", query);
		String productList = buildProductList(recommendations);

		PromptTemplate promptTemplate = new PromptTemplate(
			new ClassPathResource("prompts/recommendation.st"));

		String finalPrompt = promptTemplate.render(Map.of(
			"searchContext", searchContext,
			"userContext", userContext,
			"productList", productList));

		try {
			return chatClient.prompt()
				.system(finalPrompt)
				.user(query + "를 검색한 상황이야.")
				.call()
				.content();
		} catch (Exception e) {
			log.error("추천 메시지 생성 실패", e);
			return null;
		}
	}

	private String buildUserContext(String userId) {
		UserContext ctx = userContextService.getUserContext(userId);
		if (ctx == null) {
			return "";
		}
		return String.format(
			"사용자 정보: [성별: %s, 나이: %d세, 최근 검색어: %s, 장바구니 상품: %s]",
			ctx.gender(), ctx.age(),
			String.join(", ", ctx.recentSearchKeywords()),
			String.join(", ", ctx.cartProductNames()));
	}

	private String buildProductList(List<DocumentSearchResponse> recommendations) {
		StringBuilder productList = new StringBuilder();
		for (DocumentSearchResponse doc : recommendations) {
			String productId = (String)doc.metadata().get("productId");
			Integer price = (Integer)doc.metadata().get("price");
			String url = productBaseUrl + "/" + productId;
			productList.append(String.format("- 가격: %d원, URL: %s\n", price, url));
		}
		return productList.toString();
	}
}
