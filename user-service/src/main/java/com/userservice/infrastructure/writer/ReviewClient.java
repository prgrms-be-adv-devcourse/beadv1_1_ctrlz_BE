package com.userservice.infrastructure.writer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.userservice.infrastructure.feign.configuration.ProfileImageClientConfiguration;

@FeignClient(
	name = "review-service",
	url = "localhost:8081",
	configuration = {ProfileImageClientConfiguration.class}
)
public interface ReviewClient {

	@GetMapping(value = "/api/reviews/test")
	String feignClientErrorTestApi();
}
