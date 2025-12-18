package com.aiservice.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.aiservice.domain.model.RecommendationResult;
import com.aiservice.domain.vo.RecommendationStatus;

@ExtendWith(MockitoExtension.class)
class RedisSessionProviderTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisSessionProvider redisSessionProvider;

    @Test
    @DisplayName("test1: 추천 데이터 발행 및 저장 확인")
    void test1() {
        // given
        String userId = "testUser";
        RecommendationResult result = RecommendationResult.builder()
                .status(RecommendationStatus.OK)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        redisSessionProvider.publishRecommendationData(userId, result);

        // then
        verify(valueOperations).set(eq("recommend:user:" + userId), eq(result), any(Duration.class));
        verify(redisTemplate).convertAndSend(eq("recommendation:" + userId), eq(result));
    }

    @Test
    @DisplayName("test2: 추천 데이터 조회 확인")
    void test2() {
        // given
        String userId = "testUser";
        RecommendationResult expectedResult = RecommendationResult.builder().status(RecommendationStatus.OK).build();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("recommend:user:" + userId)).thenReturn(expectedResult);

        // when
        RecommendationResult actualResult = redisSessionProvider.getRecommendations(userId);

        // then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("test3: 추천 횟수 증가 확인")
    void test3() {
        // given
        String userId = "testUser";
        String key = "recommend:count:" + userId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key)).thenReturn(1L);

        // when
        redisSessionProvider.incrementRecommendationCount(userId);

        // then
        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(eq(key), any(Duration.class));
    }
}
