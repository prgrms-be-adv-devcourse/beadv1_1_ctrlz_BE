package com.user.infrastructure.reader.port.dto;

import lombok.Builder;

@Builder
public record UserDemographicDescription(
	int age,
	String gender
) {
}
