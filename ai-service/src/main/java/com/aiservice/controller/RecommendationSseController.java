package com.aiservice.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.aiservice.application.SseCommunicationService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Deprecated 프론트엔드 진행 작업에 따라 사용 여부가 달라집니다.
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class RecommendationSseController {

    private final SseCommunicationService sseCommunicationService;

    @GetMapping(value = "/recommendations/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRecommendations(@PathVariable String userId) {
        return sseCommunicationService.connect(userId);
    }
}
