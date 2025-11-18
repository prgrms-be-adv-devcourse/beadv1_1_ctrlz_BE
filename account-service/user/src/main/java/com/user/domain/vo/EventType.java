package com.user.domain.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventType {
	CREATED("user is created userId: ");

	private final String value;

	public String getContentWithUserId(String userId) {
		return this.value + userId;
	}
}
