package com.domainservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.domainservice", "com.common"})
@EnableFeignClients(
	basePackages = {"com.domainservice.common.feign.client"}
)
public class DomainServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DomainServiceApplication.class, args);
	}

}
