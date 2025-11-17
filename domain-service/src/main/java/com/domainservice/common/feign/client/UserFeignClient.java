package com.domainservice.common.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.domainservice.common.feign.configuration.UserClientConfiguration;
import com.domainservice.common.model.user.UserResponse;

@FeignClient(
	name = "user-service",
	url = "localhost:8080",
	configuration = {UserClientConfiguration.class}
)
public interface UserFeignClient {

	@GetMapping("/api/users/{id}")
	UserResponse getUser(@PathVariable String id);
}
