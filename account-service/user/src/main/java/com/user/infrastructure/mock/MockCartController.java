package com.user.infrastructure.mock;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.infrastructure.feign.dto.CartCreateRequest;

@RestController
@RequestMapping("/api/carts")
public class MockCartController {

	@PostMapping
	public ResponseEntity<?> test(@RequestBody CartCreateRequest cartCreateRequest) {
		return ResponseEntity.ok().build();
	}

}
