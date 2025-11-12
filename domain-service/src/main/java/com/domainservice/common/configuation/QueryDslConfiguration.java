package com.domainservice.common.configuation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class QueryDslConfiguration {

	private final EntityManager entityManager;

	// @Bean
	// public JPAQueryFactory jpaQueryFactory(){
	// 	return new JPAQueryFactory(entityManager);
	// }
}
