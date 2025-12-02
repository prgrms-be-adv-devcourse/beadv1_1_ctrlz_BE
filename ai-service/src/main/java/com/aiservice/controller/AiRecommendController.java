package com.aiservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiservice.controller.dto.RecommendResponse;

@RestController
@RequestMapping("/api/ai-recommend")
public class AiRecommendController {

	@GetMapping
	public RecommendResponse getRecommend(
		@RequestHeader("X-REQUEST-ID") String userId
	) {
		return null;
	}
}
