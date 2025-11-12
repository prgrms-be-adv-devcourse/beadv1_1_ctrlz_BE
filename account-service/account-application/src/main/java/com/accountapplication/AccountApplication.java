package com.accountapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
	scanBasePackages = {
		"com.user", "com.auth", "com.accountapplication"
	}
)
@EntityScan(basePackages = {"com.user", "com.auth", "com.accountapplication"})
@EnableJpaRepositories(basePackages = {"com.user", "com.auth"})
public class AccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountApplication.class, args);
	}
}
