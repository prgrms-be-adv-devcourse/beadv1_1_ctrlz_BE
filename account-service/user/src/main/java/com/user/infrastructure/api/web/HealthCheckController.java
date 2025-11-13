package com.user.infrastructure.api.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

	private final Environment environment;

	@Value("${POD_IP:unknown}")
	private String podIp;

	@GetMapping
	public String healthCheck() {
		return "현재 설정된 pod ip: %s".formatted(podIp);
	}
}
