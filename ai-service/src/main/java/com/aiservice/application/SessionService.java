package com.aiservice.application;

import com.aiservice.domain.model.RecommendationResult;

public interface SessionService {
	
    void publishRecommendationData(String userId, RecommendationResult result);

    RecommendationResult getRecommendations(String userId);

    void incrementRecommendationCount(String userId);

    int getRecommendationCount(String userId);
}
