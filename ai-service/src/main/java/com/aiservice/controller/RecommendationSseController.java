package com.aiservice.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.aiservice.application.RecommendationSseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE 엔드포인트: Redis Pub/Sub에서 추천 결과를 구독하여 클라이언트에게 전달 (MVC 방식)
 */
@Slf4j
@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class RecommendationSseController {

    private final RecommendationSseService recommendationSseService;

    @GetMapping(value = "/recommendations/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRecommendations(@PathVariable String userId) {
        return recommendationSseService.connect(userId);
    }
}
