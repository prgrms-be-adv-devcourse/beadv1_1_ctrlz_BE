
package com.aiservice.application;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.RecommendationResult;
import com.aiservice.domain.vo.RecommendationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductRecommendationService implements RecommendService {

	@Value("${custom.recommendation.limit:10}")
	private int recommendationLimit;

	private final SessionService sessionService;
	private final HybridSearchProcessor hybridSearchProcessor;
	private final RecommendationMessageGenerator recommendationMessageGenerator;

	@Override
	public void recommendProductsByQuery(String userId, String query) {
		log.info("추천 생성 시작 - 사용자: {}, 쿼리: {}", userId, query);

		// 1. 추천 제한 체크
		if (isLimitReached(userId)) {
			log.info("사용자 추천 제한 ({}) 도달: {}", recommendationLimit, userId);
			sessionService.publishRecommendationData(userId, RecommendationResult.limitReached());
			return;
		}

		// 2. 하이브리드 검색
		List<DocumentSearchResponse> searchResults = hybridSearchProcessor.search(query, 20);

		// 3. 메시지 생성 및 결과 구성
		RecommendationResult result = buildResult(userId, query, searchResults);

		// 4. 세션에 발행
		sessionService.publishRecommendationData(userId, result);
		sessionService.incrementRecommendationCount(userId);
		log.info("{} 개의 추천 결과 저장 완료 - 사용자: {} (쿼리: {})", searchResults.size(), userId, query);
	}

	private boolean isLimitReached(String userId) {
		return sessionService.getRecommendationCount(userId) >= recommendationLimit;
	}

	private RecommendationResult buildResult(String userId, String query,
			List<DocumentSearchResponse> searchResults) {
		return Optional.ofNullable(recommendationMessageGenerator.toPrompt(userId, query, searchResults))
				.map(msg -> RecommendationResult.builder()
						.status(RecommendationStatus.OK)
						.message(msg)
						.items(searchResults)
						.build())
				.orElseGet(RecommendationResult::noResults);
	}
}
