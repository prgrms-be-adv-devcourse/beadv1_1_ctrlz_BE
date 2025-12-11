package com.domainservice.domain.search.api;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.search.repository.redis.PopularSearchWordRedisRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/searches/redis-test")
public class RedisTestController {

	private final PopularSearchWordRedisRepository popularSearchWordRedisRepository;
	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * Redis에 저장된 모든 키갑 조회 api
	 * @return
	 */
	@GetMapping("/all")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<Object> getRedisContent() {
		Object response = popularSearchWordRedisRepository.findAll();
		return new BaseResponse<>(
			response,
			""
		);
	}

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
