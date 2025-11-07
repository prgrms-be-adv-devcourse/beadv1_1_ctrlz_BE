package com.userservice.infrastructure.jpa.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan({"com.common.asset.image", "com.userservice.infrastructure"})
@EnableJpaRepositories({"com.common.asset.image", "com.userservice.infrastructure"})
@EnableJpaAuditing
@Configuration
public class JpaConfiguration {
}
