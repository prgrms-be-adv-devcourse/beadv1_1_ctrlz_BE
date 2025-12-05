package com.aiservice.application;

import com.aiservice.domain.model.RecommendationResult;

public interface SessionService {
	
    void publishRecommandationData(String userId, RecommendationResult result);

    RecommendationResult getRecommendations(String userId);

    void incrementRecommendationCount(String userId);

    long getRecommendationCount(String userId);
}
