package com.aiservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiservice.application.SessionService;
import com.aiservice.controller.dto.BaseResponse;
import com.aiservice.domain.model.RecommendationResult;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Deprecated 프론트엔드 진행 작업에 따라 사용 여부가 달라집니다.
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationPollingController {

	private final SessionService sessionService;

	@GetMapping("/{userId}")
	public BaseResponse<RecommendationResult> getRecommendations(@PathVariable String userId) {
		log.info("추천 데이터 조회 요청 (Polling) - 사용자: {}", userId);

		RecommendationResult result = sessionService.getRecommendations(userId);

		log.info("추천 데이터 반환 - 사용자: {}, 상태: {}", userId, result.status());
		return new BaseResponse<>(result, "추천 데이터 반환");
	}
}
