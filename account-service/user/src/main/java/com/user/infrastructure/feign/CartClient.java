package com.user.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.user.infrastructure.feign.dto.CartCreateRequest;

@FeignClient(name = "cart-service", url = "localhost:8081")
public interface CartClient {

	@PostMapping("/api/carts")
	ResponseEntity<Void> createCart(@RequestBody CartCreateRequest cartCreateRequest);
}
