package com.user.application.adapter.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventType {
	CREATED("user is created userId: ");

	private final String value;

	public String getContentWithUserId(String userId) {
		return this.value + userId;
	}
}
