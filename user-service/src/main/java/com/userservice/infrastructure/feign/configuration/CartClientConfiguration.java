package com.userservice.infrastructure.feign.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.userservice.infrastructure.feign.exception.decoder.DefaultFeignClientErrorDecoder;

import feign.Logger;
import feign.codec.ErrorDecoder;

@Configuration
public class CartClientConfiguration {

	@Bean(name = "cartFeignLoggerLevel")
	Logger.Level feignLoggerLevel() {
		//헤더에 토큰을 담아서 요청을 보낼수도 있을거 같아서 헤더로 했습니다
		/*
		TODO:
		 FeignClient 사용 후 남는 로그 확인 후 Header가 불필요해지면 BASIC 수정 필요.
		 필요에 따라 CustomLogger 구현
		*/
		return Logger.Level.BASIC;
	}

	@Bean
	public ErrorDecoder cartClientErrorDecoder() {
		return new DefaultFeignClientErrorDecoder();
	}
}
