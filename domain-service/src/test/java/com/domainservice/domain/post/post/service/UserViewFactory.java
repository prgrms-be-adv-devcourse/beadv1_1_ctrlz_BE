package com.domainservice.domain.post.post.service;

import com.domainservice.common.model.user.UserResponse;

import java.util.List;

/**
 * 테스트용 UserResponse 팩토리 클래스
 * 반복적인 UserResponse 생성을 간편하게 해줍니다.
 */
public class UserViewFactory {

    // SELLER 권한을 가진 기본 사용자
    public static UserResponse createSeller(String userId) {
        return new UserResponse(
                "홍길동",
                "seller" + userId,
                "010-1234-5678",
                "12345",
                "서울시",
                "강남구",
                "역삼동",
                "101호",
                "seller@example.com",
                List.of("SELLER", "USER"),
                "https://example.com/profile.jpg",
                "image-123"
        );
    }

    // ADMIN 권한을 가진 사용자
    public static UserResponse createAdmin(String userId) {
        return new UserResponse(
                "관리자",
                "admin",
                "010-9999-9999",
                "12345",
                "서울시",
                "강남구",
                "역삼동",
                "관리자실",
                "admin@example.com",
                List.of("ADMIN", "USER"),
                "https://example.com/admin.jpg",
                "admin-image-123"
        );
    }

    // USER 권한만 가진 일반 사용자 (SELLER X)
    public static UserResponse createUser(String userId) {
        return new UserResponse(
                "김철수",
                "user" + userId,
                "010-5555-5555",
                "54321",
                "부산시",
                "해운대구",
                "우동",
                "201호",
                "user@example.com",
                List.of("USER"),
                "https://example.com/user.jpg",
                "user-image-123"
        );
    }
}