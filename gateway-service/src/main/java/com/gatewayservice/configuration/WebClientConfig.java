package com.gatewayservice.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced  //lb://SERVICE-NAME 형태의 URL을 호출할 수 있게 만들어주는 어노테이션
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
