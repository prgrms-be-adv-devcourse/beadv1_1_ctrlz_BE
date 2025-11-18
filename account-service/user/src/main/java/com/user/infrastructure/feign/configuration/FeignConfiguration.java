package com.user.infrastructure.feign.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.user.infrastructure.feign")
public class FeignConfiguration {
}
