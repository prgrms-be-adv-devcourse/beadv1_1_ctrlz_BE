package com.user.infrastructure.scheduler.configuration.vo;

import com.user.domain.vo.EventType;

public record PendingEventSpec(String userId, EventType eventType) {
}
