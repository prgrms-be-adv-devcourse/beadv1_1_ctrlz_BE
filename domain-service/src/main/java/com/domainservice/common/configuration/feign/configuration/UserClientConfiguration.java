package com.domainservice.common.configuration.feign.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.domainservice.common.configuration.feign.decoder.UserErrorDecoder;

import feign.Logger;
import feign.codec.ErrorDecoder;

@Configuration
public class UserClientConfiguration {

	@Bean
	Logger.Level level() {
		return Logger.Level.HEADERS;
	}

	@Bean(name = "userErrorDecoder")
	public ErrorDecoder userErrorDecoder() {
		return new UserErrorDecoder();
	}
}
