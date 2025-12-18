package com.aiservice.application;

import com.aiservice.domain.model.RecommendationResult;

public interface RecommendService {
	RecommendationResult recommendProductsByQuery(String userId, String query);
}
