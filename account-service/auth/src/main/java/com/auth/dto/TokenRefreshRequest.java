package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record TokenRefreshRequest (
	String refreshToken,
	String userId
){}
