package com.aiservice.infrastructure.feign.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackages = {"com.aiservice.infrastructure.feign"})
@Configuration
public class FeignConfiguration {
}
