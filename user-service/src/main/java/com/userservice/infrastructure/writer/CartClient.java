package com.userservice.infrastructure.writer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.userservice.infrastructure.writer.dto.CartCreateRequest;

@FeignClient(name = "domain-service", url = "localhost:8080")
public interface CartClient {

	@PostMapping("/api/carts")
	ResponseEntity<?> createCart(@RequestBody CartCreateRequest cartCreateRequest);
}
