package com.domainservice.domain.order.model.dto;

import java.time.LocalDateTime;

public record OrderedAt(
	LocalDateTime date
) {
}
