package com.aiservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aiservice.application.RecommendService;
import com.aiservice.controller.dto.BaseResponse;
import com.aiservice.domain.model.RecommendationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 검색어 기반으로 추천(LLM Trigger)을 수행하는 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationCommandController {

	private final RecommendService recommendService;

	@GetMapping
	public BaseResponse<RecommendationResult> triggerRecommendation(
			@RequestParam("query") String query,
			@RequestHeader("X-REQUEST-ID") String userId) {
		log.info("추천 생성 요청 (Command) - 사용자: {}, 쿼리: {}", userId, query);

		RecommendationResult result = recommendService.recommendProductsByQuery(userId, query);

		return new BaseResponse<>(result, "추천 생성 요청이 처리되었습니다.");
	}
}
