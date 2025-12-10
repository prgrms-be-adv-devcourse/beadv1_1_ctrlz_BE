package com.aiservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiservice.application.RecommendService;
import com.aiservice.controller.dto.BaseResponse;
import com.aiservice.controller.dto.RecommendationRequest;
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

    @PostMapping("/query")
    public BaseResponse<RecommendationResult> triggerRecommendation(@RequestBody RecommendationRequest request) {
        log.info("추천 생성 요청 (Command) - 사용자: {}, 쿼리: {}", request.userId(), request.query());

        RecommendationResult result = recommendService.recommendProductsByQuery(request.userId(), request.query());

        return new BaseResponse<>(result, "추천 생성 요청이 처리되었습니다.");
    }
}
