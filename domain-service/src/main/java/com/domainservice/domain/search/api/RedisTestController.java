package com.domainservice.domain.search.api;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/searches/redis-test")
public class RedisTestController {

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 검색어와 관련된 모든 키 삭제.
	 * @return
	 */
	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<Object> removeAll() {
		// 검색어 관련된 키 스캔
		Set<String> keys = redisTemplate.keys("search:*");
		// Set<String> keys = redisTemplate.keys("*");

		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys); // 여러 키 삭제
		}

		return new BaseResponse<>(
			keys,
			"Redis 모든 키 삭제 완료"
		);
	}
}
