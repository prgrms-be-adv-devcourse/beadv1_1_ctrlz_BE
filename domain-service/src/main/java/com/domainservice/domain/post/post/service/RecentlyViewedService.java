package com.domainservice.domain.post.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecentlyViewedService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RECENTLY_VIEW_KEY = "user:%s:view_posts";
    private static final long SECONDS_IN_A_WEEK = 7L * 24 * 60 * 60;    // 최근 7일

    /**
     * 사용자가 최근 본 게시물을 Redis ZSet(sorted set)에 추가하고 maxCount 개수만큼만 유지합니다.
     *
     * @param userId   사용자 ID
     * @param postId   게시물 ID
     * @param maxCount 유지할 최대 게시물 개수
     */
    public void addRecentlyViewedPost(String userId, String postId, int maxCount) {

        String key = getUserViewedKey(userId);

        // 현재시간을 score 값으로 저장, 추후에 시간순으로 정렬에 쓰임
        double score = getCurrentTimeInSeconds();

        redisTemplate.opsForZSet().add(key, String.valueOf(postId), score);

        /*
        최근 maxCount 개수 까지만 게시물 유지
        ex) maxCount=3 -> (가장오래전에 본 게시물) ~ (-4번째) 까지 삭제하고 3개만 남김
         */
        int removeEndIndex = -1 * (maxCount + 1);
        redisTemplate.opsForZSet().removeRange(key, 0, removeEndIndex);

        // 7일간 조회가 없었던 게시물은 삭제
        redisTemplate.expire(key, Duration.ofDays(7));

    }

    /**
     * 사용자가 최근 본 게시물 목록을 조회합니다.
     * redis에 저장된 게시물들의 id값을 list 형태로 가져옵니다.
     * 이때 reverseRangeByScore 를 사용하여 score 값 기준으로 역순 정렬(최신순) 하여 가져오게 됩니다.
     */
    public Set<String> getRecentlyViewedPostIds(String userId, int maxCount) {

        // userId로 고유한 key 값 생성
        String key = getUserViewedKey(userId);

        // 조회될 score 범위의 최소값 (현재 시간 - 7일 => 7일전까지 조회)
        double minScore = getCurrentTimeInSeconds() - SECONDS_IN_A_WEEK;

        // reverseRangeByScore : score 범위로 조회하되 score 기준으로 역순 정렬 (최신순)
        return redisTemplate.opsForZSet()
                .reverseRangeByScore(key, minScore, Double.MAX_VALUE, 0, maxCount);

    }

    private String getUserViewedKey(String userId) {
        return RECENTLY_VIEW_KEY.formatted(userId);
    }

    private double getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }

}