package com.auth.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.user.domain.vo.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

	private String id;              // Redis Key or DB Primary Key
	private String userId;
	private String provider;
	private List<UserRole> roles;
	private String token;           // Refresh Token 값
	private LocalDateTime expiryDate;  // 만료 일시
	private LocalDateTime createdAt;   // 생성 일시

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiryDate);
	}

	public RefreshToken updateToken(String newToken, LocalDateTime newExpiryDate) {
		return RefreshToken.builder()
			.id(this.id)
			.userId(this.userId)
			.provider(this.provider)
			.roles(this.roles)
			.token(newToken)
			.expiryDate(newExpiryDate)
			.createdAt(LocalDateTime.now())
			.build();
	}
}
