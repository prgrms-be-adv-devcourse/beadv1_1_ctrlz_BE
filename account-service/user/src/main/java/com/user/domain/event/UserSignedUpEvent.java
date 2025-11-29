package com.user.domain.event;

import com.user.application.adapter.vo.EventType;

public record UserSignedUpEvent(
	String userId,
	EventType eventType
) {

	public static UserSignedUpEvent from(String userId, EventType eventType) {
		return new UserSignedUpEvent(userId, eventType);
	}
}
