package com.aiservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aiservice.application.RecommendService;
import com.aiservice.controller.dto.BaseResponse;
import com.aiservice.docs.TriggerRecommendationApiDocs;
import com.aiservice.domain.model.RecommendationResult;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Ai-Service", description = "AI 추천 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

	private final RecommendService recommendService;

	@TriggerRecommendationApiDocs
	@GetMapping
	public BaseResponse<RecommendationResult> triggerRecommendation(
			@RequestParam("query") String query,
			@RequestHeader("X-REQUEST-ID") String userId) {
		log.info("추천 생성 요청 (Command) - 사용자: {}, 쿼리: {}", userId, query);

		RecommendationResult result = recommendService.recommendProductsByQuery(userId, query);

		return new BaseResponse<>(result, "추천 생성 요청이 처리되었습니다.");
	}
}
