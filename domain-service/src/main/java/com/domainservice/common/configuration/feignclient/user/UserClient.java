package com.domainservice.common.configuration.feignclient.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.domainservice.domain.post.post.model.dto.UserView;

@FeignClient(
	name = "account-service"
	//fallbackFactory = UserClientFallbackFactory.class
)
public interface UserClient {

	@GetMapping("api/users/{userId}")
	UserView getUserById(@PathVariable("userId") String userId);
}