package com.domainservice.common.configuration.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.domainservice.common.configuration.feign.configuration.UserClientConfiguration;
import com.domainservice.common.model.user.UserResponse;

@FeignClient(
	name = "user-service",
	url = "${custom.feign.url.user-service}",
	configuration = {UserClientConfiguration.class}
)
public interface UserFeignClient {

	@GetMapping("/api/users/{id}")
	UserResponse getUser(@PathVariable(name = "id") String id);
}
