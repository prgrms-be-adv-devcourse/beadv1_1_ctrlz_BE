package com.settlement.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "com.settlement" })
@EntityScan(basePackages = { "com.settlement" })
@Configuration
public class JpaConfiguration {
}
