package com.settlement.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.cloud.openfeign.EnableFeignClients;

import feign.Request;
import feign.Retryer;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;

@EnableFeignClients(basePackages = { "com.settlement.common.feign" })
@Slf4j
@Configuration
public class FeignConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }

    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder();
    }

    /**
     * Feign 재시도 설정
     * - 최대 3회 재시도
     * - 초기 대기 시간: 1초
     * - 최대 대기 시간: 3초
     */
    @Bean
    public Retryer feignRetryer() {
        log.info("Feign 재시도 로직 설정: 최대 3회, 초기 대기 1초, 최대 대기 3초");
        return new Retryer.Default(1000, TimeUnit.SECONDS.toMillis(3), 3);
    }

    /**
     * Feign 타임아웃 설정
     * - 연결 타임아웃: 5초
     * - 읽기 타임아웃: 10초
     */
    @Bean
    public Request.Options feignRequestOptions() {
        log.info("Feign 타임아웃 설정: 연결 타임아웃 5초, 읽기 타임아웃 10초");
        return new Request.Options(5, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true);
    }
}
