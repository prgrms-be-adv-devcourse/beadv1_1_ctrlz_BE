package com.aiservice.infrastructure.feign.dto;

import lombok.Builder;

@Builder
public record UserDemographicDescription(
	int age,
	String gender
) {
}
