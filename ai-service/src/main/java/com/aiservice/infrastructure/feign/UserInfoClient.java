package com.aiservice.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aiservice.infrastructure.feign.dto.UserDemographicDescription;

@FeignClient(name = "user-service", url = "${custom.feign.url.account-service}")
public interface UserInfoClient {

    @GetMapping("/api/user/recommendation-info/{userId}")
	UserDemographicDescription getRecommendationInfo(@RequestParam("userId") String userId);
}
